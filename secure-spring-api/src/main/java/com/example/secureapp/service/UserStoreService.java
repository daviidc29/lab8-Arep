package com.example.secureapp.service;

import com.example.secureapp.config.AppProperties;
import com.example.secureapp.model.UserRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserStoreService {

    private static final String USERS_FILE_NAME = "users.csv";

    private final Map<String, UserRecord> users = new HashMap<>();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Path usersFile;

    public UserStoreService(AppProperties properties) {
        Path dataDir = Path.of(properties.getDataDir());
        this.usersFile = dataDir.resolve(USERS_FILE_NAME);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(usersFile.getParent());

        if (!Files.exists(usersFile)) {
            Files.writeString(usersFile, "", StandardCharsets.UTF_8);
            return;
        }

        List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                users.put(parts[0], new UserRecord(parts[0], parts[1]));
            }
        }
    }

    public synchronized void register(String username, String rawPassword) {
        String normalized = normalizeUsername(username);

        if (users.containsKey(normalized)) {
            throw new IllegalArgumentException("User already exists");
        }

        String hash = encoder.encode(rawPassword);
        users.put(normalized, new UserRecord(normalized, hash));
        persist();
    }

    public synchronized boolean authenticate(String username, String rawPassword) {
        String normalized = normalizeUsername(username);
        UserRecord user = users.get(normalized);
        return user != null && encoder.matches(rawPassword, user.passwordHash());
    }

    public synchronized Optional<UserRecord> findByUsername(String username) {
        return Optional.ofNullable(users.get(normalizeUsername(username)));
    }

    private void persist() {
        StringBuilder content = new StringBuilder();
        for (UserRecord user : users.values()) {
            content.append(user.username())
                    .append(",")
                    .append(user.passwordHash())
                    .append(System.lineSeparator());
        }

        try {
            Files.writeString(usersFile, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not persist users", e);
        }
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}

package com.example.secureapp.service;

import com.example.secureapp.config.AppProperties;
import com.example.secureapp.model.TokenRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStoreService {

    private static final String TOKENS_FILE_NAME = "tokens.csv";

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();
    private final Path tokensFile;
    private final long tokenTtlMinutes;

    public TokenStoreService(AppProperties properties) {
        Path dataDir = Path.of(properties.getDataDir());
        this.tokensFile = dataDir.resolve(TOKENS_FILE_NAME);
        this.tokenTtlMinutes = properties.getSecurity().getTokenTtlMinutes();
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(tokensFile.getParent());

        if (!Files.exists(tokensFile)) {
            Files.writeString(tokensFile, "", StandardCharsets.UTF_8);
            return;
        }

        for (String line : Files.readAllLines(tokensFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split(",", 3);
            if (parts.length != 3) {
                continue;
            }

            Instant expiresAt = Instant.ofEpochMilli(Long.parseLong(parts[2]));
            if (expiresAt.isAfter(Instant.now())) {
                tokens.put(parts[0], new TokenRecord(parts[0], parts[1], expiresAt));
            }
        }

        persist();
    }

    public synchronized TokenRecord createToken(String username) {
        pruneExpired();

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Instant expiresAt = Instant.now().plusSeconds(tokenTtlMinutes * 60);
        TokenRecord record = new TokenRecord(token, username, expiresAt);
        tokens.put(token, record);
        persist();
        return record;
    }

    public synchronized Optional<String> validateAndGetUsername(String token) {
        pruneExpired();
        TokenRecord record = tokens.get(token);

        if (record == null || record.expiresAt().isBefore(Instant.now())) {
            tokens.remove(token);
            persist();
            return Optional.empty();
        }

        return Optional.of(record.username());
    }

    public synchronized void revoke(String token) {
        tokens.remove(token);
        persist();
    }

    private void pruneExpired() {
        Instant now = Instant.now();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private void persist() {
        StringBuilder content = new StringBuilder();
        for (TokenRecord record : tokens.values()) {
            content.append(record.token())
                    .append(",")
                    .append(record.username())
                    .append(",")
                    .append(record.expiresAt().toEpochMilli())
                    .append(System.lineSeparator());
        }

        try {
            Files.writeString(tokensFile, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not persist tokens", e);
        }
    }
}

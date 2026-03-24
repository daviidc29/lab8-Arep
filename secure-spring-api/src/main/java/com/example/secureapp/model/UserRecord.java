package com.example.secureapp.model;

public class UserRecord {

    private final String username;
    private final String passwordHash;

    public UserRecord(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }
}

package com.example.secureapp.model;

import java.time.Instant;

public class TokenRecord {

    private final String token;
    private final String username;
    private final Instant expiresAt;

    public TokenRecord(String token, String username, Instant expiresAt) {
        this.token = token;
        this.username = username;
        this.expiresAt = expiresAt;
    }

    public String token() {
        return token;
    }

    public String username() {
        return username;
    }

    public Instant expiresAt() {
        return expiresAt;
    }
}

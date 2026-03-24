package com.example.secureapp.dto;

public class AuthResponse {

    private final String username;
    private final String token;
    private final String expiresAt;
    private final String message;

    public AuthResponse(String username, String token, String expiresAt, String message) {
        this.username = username;
        this.token = token;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getMessage() {
        return message;
    }
}

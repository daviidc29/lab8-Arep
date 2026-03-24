package com.example.secureapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Pattern(regexp = "^[a-zA-Z0-9._@-]{3,60}$", message = "username contains invalid characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 128, message = "password must have between 8 and 128 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

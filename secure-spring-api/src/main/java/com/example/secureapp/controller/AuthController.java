package com.example.secureapp.controller;

import com.example.secureapp.dto.AuthResponse;
import com.example.secureapp.dto.LoginRequest;
import com.example.secureapp.dto.MessageResponse;
import com.example.secureapp.dto.RegisterRequest;
import com.example.secureapp.model.TokenRecord;
import com.example.secureapp.service.TokenStoreService;
import com.example.secureapp.service.UserStoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserStoreService userStoreService;
    private final TokenStoreService tokenStoreService;

    public AuthController(UserStoreService userStoreService, TokenStoreService tokenStoreService) {
        this.userStoreService = userStoreService;
        this.tokenStoreService = tokenStoreService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            userStoreService.register(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("User registered successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        boolean validCredentials = userStoreService.authenticate(request.getUsername(), request.getPassword());

        if (!validCredentials) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid credentials"));
        }

        String normalizedUsername = request.getUsername().trim().toLowerCase();
        TokenRecord token = tokenStoreService.createToken(normalizedUsername);

        return ResponseEntity.ok(
                new AuthResponse(
                        normalizedUsername,
                        token.token(),
                        token.expiresAt().toString(),
                        "Login successful"
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenStoreService.revoke(authorization.substring("Bearer ".length()).trim());
        }

        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
}

package com.example.secureapp.controller;

import com.example.secureapp.dto.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SecureController {

    @GetMapping("/api/health")
    public MessageResponse health() {
        return new MessageResponse("API is running");
    }

    @GetMapping("/api/secure/hello")
    public Map<String, String> hello(HttpServletRequest request) {
        String authenticatedUser = (String) request.getAttribute("authenticatedUser");
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Secure endpoint reached successfully");
        response.put("user", authenticatedUser);
        response.put("time", Instant.now().toString());
        return response;
    }
}

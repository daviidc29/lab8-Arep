package com.example.secureapp.config;

import com.example.secureapp.service.TokenStoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenStoreService tokenStoreService;

    public AuthInterceptor(TokenStoreService tokenStoreService) {
        this.tokenStoreService = tokenStoreService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Missing Bearer token");
            return false;
        }

        String rawToken = authorization.substring(BEARER_PREFIX.length()).trim();
        Optional<String> username = tokenStoreService.validateAndGetUsername(rawToken);

        if (username.isEmpty()) {
            writeUnauthorized(response, "Invalid or expired token");
            return false;
        }

        request.setAttribute("authenticatedUser", username.get());
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}

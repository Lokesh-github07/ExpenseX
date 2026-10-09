package com.example.expensetracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                401
        );

        body.put(
                "error",
                "Unauthorized"
        );

        body.put(
                "message",
                authException.getMessage()
        );

        body.put(
                "path",
                request.getServletPath()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}
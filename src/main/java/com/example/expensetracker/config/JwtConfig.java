package com.example.expensetracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpiration;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}
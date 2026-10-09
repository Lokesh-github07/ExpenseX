package com.example.expensetracker.util;

import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public final class JwtUtil {

    private JwtUtil() {
    }

    public static String getUsername(
            Claims claims) {

        return claims.getSubject();
    }

    public static Date getExpirationDate(
            Claims claims) {

        return claims.getExpiration();
    }

    public static <T> T getClaim(
            Claims claims,
            Function<Claims, T> resolver) {

        return resolver.apply(claims);
    }

    public static boolean isTokenExpired(
            Claims claims) {

        return getExpirationDate(claims)
                .before(new Date());
    }

    public static Long getUserId(
            Claims claims) {

        Object userId =
                claims.get("userId");

        if (userId == null) {
            return null;
        }

        return Long.valueOf(
                userId.toString()
        );
    }

    public static String getRole(
            Claims claims) {

        Object role =
                claims.get("role");

        return role != null
                ? role.toString()
                : null;
    }
}
package com.example.expensetracker.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    public static boolean isValidEmail(
            String email) {

        return email != null
                && EMAIL_PATTERN
                .matcher(email)
                .matches();
    }

    public static boolean isValidPassword(
            String password) {

        return password != null
                && password.length() >= 8;
    }

    public static boolean isValidAmount(
            BigDecimal amount) {

        return amount != null
                && amount.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    public static boolean isNullOrEmpty(
            String value) {

        return value == null
                || value.trim().isEmpty();
    }

    public static void validateExpenseAmount(
            BigDecimal amount) {

        if (!isValidAmount(amount)) {

            throw new IllegalArgumentException(
                    "Expense amount must be greater than zero"
            );
        }
    }

    public static void validateEmail(
            String email) {

        if (!isValidEmail(email)) {

            throw new IllegalArgumentException(
                    "Invalid email address"
            );
        }
    }

    public static void validatePassword(
            String password) {

        if (!isValidPassword(password)) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }
    }
}
package com.example.expensetracker.util;

public final class Constants {

    private Constants() {
    }

    // Success Messages
    public static final String USER_REGISTERED_SUCCESS =
            "User registered successfully";

    public static final String LOGIN_SUCCESS =
            "Login successful";

    public static final String PASSWORD_RESET_SUCCESS =
            "Password reset successfully";

    public static final String PASSWORD_RESET_EMAIL_SENT =
            "Password reset email sent successfully";

    public static final String CATEGORY_CREATED =
            "Category created successfully";

    public static final String CATEGORY_UPDATED =
            "Category updated successfully";

    public static final String CATEGORY_DELETED =
            "Category deleted successfully";

    public static final String EXPENSE_CREATED =
            "Expense created successfully";

    public static final String EXPENSE_UPDATED =
            "Expense updated successfully";

    public static final String EXPENSE_DELETED =
            "Expense deleted successfully";

    public static final String BUDGET_CREATED =
            "Budget created successfully";

    public static final String BUDGET_UPDATED =
            "Budget updated successfully";

    public static final String BUDGET_DELETED =
            "Budget deleted successfully";

    // Error Messages
    public static final String USER_NOT_FOUND =
            "User not found";

    public static final String CATEGORY_NOT_FOUND =
            "Category not found";

    public static final String EXPENSE_NOT_FOUND =
            "Expense not found";

    public static final String BUDGET_NOT_FOUND =
            "Budget not found";

    public static final String INVALID_CREDENTIALS =
            "Invalid email or password";

    public static final String EMAIL_ALREADY_EXISTS =
            "Email already exists";

    public static final String INVALID_TOKEN =
            "Invalid token";

    public static final String TOKEN_EXPIRED =
            "Token expired";

    public static final String ACCESS_DENIED =
            "Access denied";

    // Roles
    public static final String ROLE_USER =
            "ROLE_USER";

    public static final String ROLE_ADMIN =
            "ROLE_ADMIN";

    // JWT
    public static final String TOKEN_PREFIX =
            "Bearer ";

    public static final String AUTHORIZATION_HEADER =
            "Authorization";

    // Mail
    public static final String RESET_PASSWORD_SUBJECT =
            "Expense Tracker Password Reset";

    // Pagination
    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 10;

    // Date Format
    public static final String DATE_FORMAT =
            "dd-MM-yyyy";

    public static final String DATE_TIME_FORMAT =
            "dd-MM-yyyy HH:mm:ss";
}
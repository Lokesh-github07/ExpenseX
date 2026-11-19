package com.example.expensetracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull(message = "Month is required")
    private Integer month;

    @NotNull(message = "Year is required")
    private Integer year;

    // Kept only for backward compatibility with existing clients.
    // It is NOT trusted: the server always uses the authenticated user.
    // If supplied, it must equal the caller's own ID (otherwise 403).
    private Long userId;
}
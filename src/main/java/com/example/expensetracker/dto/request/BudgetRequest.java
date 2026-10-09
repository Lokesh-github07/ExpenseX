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

    @NotNull(message = "User ID is required")
    private Long userId;
}
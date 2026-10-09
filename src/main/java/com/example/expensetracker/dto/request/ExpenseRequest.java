package com.example.expensetracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "User ID is required")
    private Long userId;
}
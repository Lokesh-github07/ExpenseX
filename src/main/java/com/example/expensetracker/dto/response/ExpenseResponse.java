package com.example.expensetracker.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseResponse {

    private Long id;

    private String title;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private String categoryName;

    private Long categoryId;

    private Long userId;
}
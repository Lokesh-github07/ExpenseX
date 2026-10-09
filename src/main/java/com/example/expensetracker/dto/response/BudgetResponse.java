package com.example.expensetracker.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetResponse {

    private Long id;

    private BigDecimal amount;

    private Integer month;

    private Integer year;

    private Long userId;
}
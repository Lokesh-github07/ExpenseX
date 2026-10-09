package com.example.expensetracker.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardResponse {

    private BigDecimal totalExpense;

    private BigDecimal totalBudget;

    private BigDecimal remainingBudget;

    private Long totalTransactions;

    private Long totalCategories;
}
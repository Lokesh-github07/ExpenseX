package com.example.expensetracker.service;

import com.example.expensetracker.dto.request.ExpenseRequest;
import com.example.expensetracker.dto.response.ExpenseResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(
            ExpenseRequest request);

    List<ExpenseResponse> getAllExpenses();

    ExpenseResponse getExpenseById(Long id);

    ExpenseResponse updateExpense(
            Long id,
            ExpenseRequest request);

    void deleteExpense(Long id);

    List<ExpenseResponse> getExpensesByCategory(
            Long categoryId);

    List<ExpenseResponse> getExpensesBetweenDates(
            LocalDate startDate,
            LocalDate endDate);

    BigDecimal getTotalExpense();

    BigDecimal getMonthlyExpense(
            int month,
            int year);
}
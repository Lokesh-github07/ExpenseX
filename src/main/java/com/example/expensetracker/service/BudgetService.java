package com.example.expensetracker.service;

import com.example.expensetracker.dto.request.BudgetRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.BudgetResponse;

import java.util.List;

public interface BudgetService {

    BudgetResponse createBudget(
            BudgetRequest request);

    List<BudgetResponse> getAllBudgets();

    BudgetResponse getBudgetById(Long id);

    BudgetResponse updateBudget(
            Long id,
            BudgetRequest request);

    ApiResponse deleteBudget(Long id);

    List<BudgetResponse> getBudgetsByUser(
            Long userId);

    BudgetResponse getBudgetByMonth(
            Integer month,
            Integer year,
            Long userId);
}
package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.response.DashboardResponse;
import com.example.expensetracker.model.Budget;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public DashboardResponse getDashboardData() {

        DashboardResponse response =
                new DashboardResponse();

        BigDecimal totalExpense =
                expenseRepository.getTotalExpense();

        response.setTotalExpense(totalExpense);
        response.setTotalTransactions(
                (long) expenseRepository.count()
        );
        response.setTotalCategories(
                (long) categoryRepository.count()
        );

        return response;
    }

    @Override
    public DashboardResponse getUserDashboard(
            Long userId) {

        DashboardResponse response =
                new DashboardResponse();

        BigDecimal totalExpense =
                expenseRepository.getTotalExpenseByUser(
                        userId
                );

        response.setTotalExpense(totalExpense);

        response.setTotalTransactions(
                expenseRepository.countByUserId(
                        userId
                )
        );

        response.setTotalCategories(
                (long) categoryRepository.count()
        );

        return response;
    }

    @Override
    public DashboardResponse getMonthlyDashboard(
            Integer month,
            Integer year,
            Long userId) {

        DashboardResponse response =
                new DashboardResponse();

        BigDecimal monthlyExpense =
                expenseRepository.getMonthlyExpenseByUser(
                        userId,
                        month,
                        year
                );

        Budget budget =
                budgetRepository
                        .findByUserIdAndMonthAndYear(
                                userId,
                                month,
                                year
                        )
                        .orElse(null);

        BigDecimal budgetAmount =
                budget != null
                        ? budget.getAmount()
                        : BigDecimal.ZERO;

        response.setTotalExpense(
                monthlyExpense
        );

        response.setTotalBudget(
                budgetAmount
        );

        response.setRemainingBudget(
                budgetAmount.subtract(
                        monthlyExpense
                )
        );

        return response;
    }

    @Override
    public DashboardResponse getSummary(
            Long userId) {

        LocalDate now = LocalDate.now();

        return getMonthlyDashboard(
                now.getMonthValue(),
                now.getYear(),
                userId
        );
    }
}
package com.example.expensetracker.service;

import com.example.expensetracker.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboardData();

    DashboardResponse getUserDashboard(Long userId);

    DashboardResponse getMonthlyDashboard(
            Integer month,
            Integer year,
            Long userId);

    DashboardResponse getSummary(Long userId);
}
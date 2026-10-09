package com.example.expensetracker.service;

import com.example.expensetracker.dto.response.AnalyticsResponse;

public interface ReportService {

    AnalyticsResponse getAnalytics(
            Long userId,
            Integer monthsBack);

    byte[] generatePdfReport(
            Long userId,
            Integer month,
            Integer year);

    byte[] generateExcelReport(
            Long userId,
            Integer month,
            Integer year);

    Object getMonthlySummary(
            Long userId,
            Integer month,
            Integer year);

    Object getYearlySummary(
            Long userId,
            Integer year);

    Object getCategorySummary(
            Long userId);
}
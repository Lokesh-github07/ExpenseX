package com.example.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AnalyticsResponse {

    private BigDecimal totalExpense;

    private BigDecimal averageMonthlyExpense;

    private List<CategoryBreakdownItem> categoryBreakdown;

    private List<MonthlyPoint> monthlyTrend;

    private List<BudgetVsActualPoint> budgetVsActual;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdownItem {

        private String category;

        private BigDecimal amount;

        private double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPoint {

        private Integer month;

        private Integer year;

        private String label;

        private BigDecimal total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetVsActualPoint {

        private Integer month;

        private Integer year;

        private String label;

        private BigDecimal budget;

        private BigDecimal actual;
    }
}

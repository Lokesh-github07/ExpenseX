package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.response.AnalyticsResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.ReportService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    // ============================================================
    // Analytics (feeds the analytics dashboard charts)
    // ============================================================

    @Override
    public AnalyticsResponse getAnalytics(
            Long userId,
            Integer monthsBack) {

        int months = (monthsBack == null || monthsBack < 1) ? 6 : monthsBack;

        AnalyticsResponse response = new AnalyticsResponse();

        BigDecimal totalExpense =
                expenseRepository.getTotalExpenseByUser(userId);

        response.setTotalExpense(totalExpense);

        // ---------- Category breakdown ----------
        List<Object[]> categoryRows =
                expenseRepository.getCategoryWiseExpenseByUser(userId);

        List<AnalyticsResponse.CategoryBreakdownItem> categoryBreakdown =
                new ArrayList<>();

        for (Object[] row : categoryRows) {

            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            double percentage = 0.0;

            if (totalExpense != null
                    && totalExpense.compareTo(BigDecimal.ZERO) > 0) {

                percentage = amount
                        .divide(totalExpense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            categoryBreakdown.add(
                    new AnalyticsResponse.CategoryBreakdownItem(
                            categoryName, amount, percentage
                    )
            );
        }

        response.setCategoryBreakdown(categoryBreakdown);

        // ---------- Monthly trend (last N months, including months with 0 spend) ----------
        LocalDate startDate =
                YearMonth.now().minusMonths(months - 1L).atDay(1);

        List<Object[]> trendRows =
                expenseRepository.getMonthlyTrendByUser(userId, startDate);

        Map<String, BigDecimal> trendMap = new HashMap<>();

        for (Object[] row : trendRows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal total = (BigDecimal) row[2];
            trendMap.put(year + "-" + month, total);
        }

        List<AnalyticsResponse.MonthlyPoint> monthlyTrend = new ArrayList<>();
        List<AnalyticsResponse.BudgetVsActualPoint> budgetVsActual = new ArrayList<>();

        YearMonth cursor = YearMonth.now().minusMonths(months - 1L);

        BigDecimal runningTotal = BigDecimal.ZERO;

        for (int i = 0; i < months; i++) {

            int year = cursor.getYear();
            int month = cursor.getMonthValue();
            String label = monthLabel(month, year);

            BigDecimal actual = trendMap.getOrDefault(
                    year + "-" + month, BigDecimal.ZERO
            );

            monthlyTrend.add(
                    new AnalyticsResponse.MonthlyPoint(
                            month, year, label, actual
                    )
            );

            Budget budget = budgetRepository
                    .findByUserIdAndMonthAndYear(userId, month, year)
                    .orElse(null);

            BigDecimal budgetAmount =
                    budget != null ? budget.getAmount() : BigDecimal.ZERO;

            budgetVsActual.add(
                    new AnalyticsResponse.BudgetVsActualPoint(
                            month, year, label, budgetAmount, actual
                    )
            );

            runningTotal = runningTotal.add(actual);

            cursor = cursor.plusMonths(1);
        }

        response.setMonthlyTrend(monthlyTrend);
        response.setBudgetVsActual(budgetVsActual);

        response.setAverageMonthlyExpense(
                runningTotal.divide(
                        BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP
                )
        );

        return response;
    }

    // ============================================================
    // PDF report
    // ============================================================

    @Override
    public byte[] generatePdfReport(
            Long userId,
            Integer month,
            Integer year) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        List<Expense> expenses = expensesForPeriod(userId, month, year);

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new BaseColor(37, 99, 235));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(30, 41, 59));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);

            Paragraph title = new Paragraph("Expense Report", titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph period = new Paragraph(
                    monthLabel(month, year), normalFont
            );
            period.setSpacingAfter(14);
            document.add(period);

            Paragraph userInfo = new Paragraph(
                    "Name: " + user.getFirstName() + " " + user.getLastName()
                            + "\nEmail: " + user.getEmail(),
                    normalFont
            );
            userInfo.setSpacingAfter(18);
            document.add(userInfo);

            // ---- Summary ----
            BigDecimal totalExpense = expenseRepository
                    .getMonthlyExpenseByUser(userId, month, year);

            Budget budget = budgetRepository
                    .findByUserIdAndMonthAndYear(userId, month, year)
                    .orElse(null);

            BigDecimal budgetAmount =
                    budget != null ? budget.getAmount() : BigDecimal.ZERO;

            Paragraph summaryTitle = new Paragraph("Summary", sectionFont);
            summaryTitle.setSpacingAfter(8);
            document.add(summaryTitle);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addSummaryRow(summaryTable, "Total Spent", "Rs. " + totalExpense, normalFont);
            addSummaryRow(summaryTable, "Budget", "Rs. " + budgetAmount, normalFont);
            addSummaryRow(summaryTable, "Remaining", "Rs. " + budgetAmount.subtract(totalExpense), normalFont);
            addSummaryRow(summaryTable, "Transactions", String.valueOf(expenses.size()), normalFont);

            document.add(summaryTable);

            // ---- Category breakdown ----
            List<Object[]> categoryRows =
                    expenseRepository.getCategoryWiseExpenseByUser(userId);

            if (!categoryRows.isEmpty()) {

                Paragraph categoryTitle = new Paragraph("Category Breakdown", sectionFont);
                categoryTitle.setSpacingAfter(8);
                document.add(categoryTitle);

                PdfPTable categoryTable = new PdfPTable(2);
                categoryTable.setWidthPercentage(100);
                categoryTable.setSpacingAfter(20);

                addHeaderCell(categoryTable, "Category", headerFont);
                addHeaderCell(categoryTable, "Amount", headerFont);

                for (Object[] row : categoryRows) {
                    categoryTable.addCell(new PdfPCell(new Phrase(String.valueOf(row[0]), normalFont)));
                    categoryTable.addCell(new PdfPCell(new Phrase("Rs. " + row[1], normalFont)));
                }

                document.add(categoryTable);
            }

            // ---- Expense list ----
            Paragraph expenseTitle = new Paragraph("Expenses", sectionFont);
            expenseTitle.setSpacingAfter(8);
            document.add(expenseTitle);

            PdfPTable expenseTable = new PdfPTable(4);
            expenseTable.setWidthPercentage(100);
            expenseTable.setWidths(new float[]{3, 2, 2, 2});

            addHeaderCell(expenseTable, "Title", headerFont);
            addHeaderCell(expenseTable, "Category", headerFont);
            addHeaderCell(expenseTable, "Date", headerFont);
            addHeaderCell(expenseTable, "Amount", headerFont);

            if (expenses.isEmpty()) {

                PdfPCell empty = new PdfPCell(new Phrase("No expenses recorded for this period.", normalFont));
                empty.setColspan(4);
                empty.setPadding(10);
                expenseTable.addCell(empty);

            } else {

                for (Expense e : expenses) {
                    expenseTable.addCell(new PdfPCell(new Phrase(nullSafe(e.getTitle()), normalFont)));
                    expenseTable.addCell(new PdfPCell(new Phrase(
                            e.getCategory() != null ? e.getCategory().getName() : "Uncategorized", normalFont)));
                    expenseTable.addCell(new PdfPCell(new Phrase(
                            e.getExpenseDate() != null ? e.getExpenseDate().toString() : "", normalFont)));
                    expenseTable.addCell(new PdfPCell(new Phrase("Rs. " + e.getAmount(), normalFont)));
                }
            }

            document.add(expenseTable);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to generate PDF report", ex
            );
        }
    }

    // ============================================================
    // Excel report
    // ============================================================

    @Override
    public byte[] generateExcelReport(
            Long userId,
            Integer month,
            Integer year) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));

        List<Expense> expenses = expensesForPeriod(userId, month, year);

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ---- Summary sheet ----
            XSSFSheet summarySheet = workbook.createSheet("Summary");

            BigDecimal totalExpense = expenseRepository
                    .getMonthlyExpenseByUser(userId, month, year);

            Budget budget = budgetRepository
                    .findByUserIdAndMonthAndYear(userId, month, year)
                    .orElse(null);

            BigDecimal budgetAmount =
                    budget != null ? budget.getAmount() : BigDecimal.ZERO;

            int r = 0;
            writeRow(summarySheet, r++, "Expense Report", null);
            writeRow(summarySheet, r++, "Name", user.getFirstName() + " " + user.getLastName());
            writeRow(summarySheet, r++, "Email", user.getEmail());
            writeRow(summarySheet, r++, "Period", monthLabel(month, year));
            r++;
            writeRow(summarySheet, r++, "Total Spent", totalExpense != null ? totalExpense.doubleValue() : 0);
            writeRow(summarySheet, r++, "Budget", budgetAmount.doubleValue());
            writeRow(summarySheet, r++, "Remaining", budgetAmount.subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO).doubleValue());
            writeRow(summarySheet, r++, "Transactions", expenses.size());

            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            // ---- Category breakdown sheet ----
            XSSFSheet categorySheet = workbook.createSheet("Category Breakdown");

            Row catHeader = categorySheet.createRow(0);
            createHeaderCell(catHeader, 0, "Category", headerStyle);
            createHeaderCell(catHeader, 1, "Amount", headerStyle);
            createHeaderCell(catHeader, 2, "Percentage", headerStyle);

            List<Object[]> categoryRows =
                    expenseRepository.getCategoryWiseExpenseByUser(userId);

            BigDecimal allTimeTotal = expenseRepository.getTotalExpenseByUser(userId);

            int cr = 1;
            for (Object[] row : categoryRows) {

                BigDecimal amount = (BigDecimal) row[1];
                double pct = 0.0;

                if (allTimeTotal != null && allTimeTotal.compareTo(BigDecimal.ZERO) > 0) {
                    pct = amount.divide(allTimeTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                }

                Row row0 = categorySheet.createRow(cr++);
                row0.createCell(0).setCellValue(String.valueOf(row[0]));
                row0.createCell(1).setCellValue(amount.doubleValue());
                row0.createCell(2).setCellValue(pct);
            }

            categorySheet.autoSizeColumn(0);
            categorySheet.autoSizeColumn(1);
            categorySheet.autoSizeColumn(2);

            // ---- Expenses sheet ----
            XSSFSheet expenseSheet = workbook.createSheet("Expenses");

            Row expHeader = expenseSheet.createRow(0);
            createHeaderCell(expHeader, 0, "Title", headerStyle);
            createHeaderCell(expHeader, 1, "Category", headerStyle);
            createHeaderCell(expHeader, 2, "Date", headerStyle);
            createHeaderCell(expHeader, 3, "Amount", headerStyle);
            createHeaderCell(expHeader, 4, "Description", headerStyle);

            int er = 1;
            for (Expense e : expenses) {

                Row row0 = expenseSheet.createRow(er++);
                row0.createCell(0).setCellValue(nullSafe(e.getTitle()));
                row0.createCell(1).setCellValue(
                        e.getCategory() != null ? e.getCategory().getName() : "Uncategorized");
                row0.createCell(2).setCellValue(
                        e.getExpenseDate() != null ? e.getExpenseDate().toString() : "");
                row0.createCell(3).setCellValue(
                        e.getAmount() != null ? e.getAmount().doubleValue() : 0);
                row0.createCell(4).setCellValue(nullSafe(e.getDescription()));
            }

            for (int c = 0; c < 5; c++) {
                expenseSheet.autoSizeColumn(c);
            }

            workbook.write(out);

            return out.toByteArray();

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to generate Excel report", ex
            );
        }
    }

    // ============================================================
    // Existing summary endpoints (unchanged behaviour)
    // ============================================================

    @Override
    public Object getMonthlySummary(
            Long userId,
            Integer month,
            Integer year) {

        Map<String, Object> summary = new HashMap<>();

        summary.put("userId", userId);
        summary.put("month", month);
        summary.put("year", year);
        summary.put("totalExpense",
                expenseRepository.getMonthlyExpenseByUser(userId, month, year));

        return summary;
    }

    @Override
    public Object getYearlySummary(
            Long userId,
            Integer year) {

        Map<String, Object> summary = new HashMap<>();

        summary.put("userId", userId);
        summary.put("year", year);
        summary.put("totalExpense",
                expenseRepository.getTotalExpenseByUser(userId));

        return summary;
    }

    @Override
    public Object getCategorySummary(
            Long userId) {

        Map<String, Object> summary = new HashMap<>();

        summary.put("userId", userId);

        BigDecimal totalExpense =
                expenseRepository.getTotalExpenseByUser(userId);

        summary.put("totalExpense", totalExpense);

        List<Object[]> rows =
                expenseRepository.getCategoryWiseExpenseByUser(userId);

        List<Map<String, Object>> breakdown = new ArrayList<>();

        for (Object[] row : rows) {

            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            double percentage = 0.0;

            if (totalExpense != null
                    && totalExpense.compareTo(BigDecimal.ZERO) > 0) {

                percentage = amount
                        .divide(totalExpense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            Map<String, Object> entry = new HashMap<>();
            entry.put("category", categoryName);
            entry.put("amount", amount);
            entry.put("percentage", percentage);

            breakdown.add(entry);
        }

        summary.put("breakdown", breakdown);

        return summary;
    }

    // ============================================================
    // Helpers
    // ============================================================

    private List<Expense> expensesForPeriod(Long userId, Integer month, Integer year) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return expenseRepository
                .findByUserIdAndExpenseDateBetweenOrderByExpenseDateAsc(userId, start, end);
    }

    private String monthLabel(Integer month, Integer year) {

        if (month == null || year == null) {
            return "";
        }

        return MONTH_NAMES[month - 1] + " " + year;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {

        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorderColor(BaseColor.LIGHT_GRAY);
        labelCell.setPadding(6);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorderColor(BaseColor.LIGHT_GRAY);
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(37, 99, 235));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void writeRow(Sheet sheet, int rowIndex, String label, Object value) {

        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);

        if (value != null) {
            Cell cell = row.createCell(1);
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(String.valueOf(value));
            }
        }
    }

    private void createHeaderCell(Row row, int col, String text, CellStyle style) {

        Cell cell = row.createCell(col);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }
}

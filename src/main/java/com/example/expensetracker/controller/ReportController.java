package com.example.expensetracker.controller;

import com.example.expensetracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "6") Integer months) {

        return ResponseEntity.ok(
                reportService.getAnalytics(userId, months)
        );
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        byte[] pdf = reportService.generatePdfReport(
                userId,
                month,
                year
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expense-report.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcelReport(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        byte[] excel = reportService.generateExcelReport(
                userId,
                month,
                year
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=expense-report.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(excel);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<?> getMonthlySummary(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {

        return ResponseEntity.ok(
                reportService.getMonthlySummary(
                        userId,
                        month,
                        year
                )
        );
    }

    @GetMapping("/yearly-summary")
    public ResponseEntity<?> getYearlySummary(
            @RequestParam Long userId,
            @RequestParam Integer year) {

        return ResponseEntity.ok(
                reportService.getYearlySummary(
                        userId,
                        year
                )
        );
    }

    @GetMapping("/category-summary")
    public ResponseEntity<?> getCategorySummary(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                reportService.getCategorySummary(userId)
        );
    }
}
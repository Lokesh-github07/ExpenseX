package com.example.expensetracker.controller;

import com.example.expensetracker.dto.response.DashboardResponse;
import com.example.expensetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData() {

        return ResponseEntity.ok(
                dashboardService.getDashboardData()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardResponse> getUserDashboard(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                dashboardService.getUserDashboard(userId)
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<DashboardResponse> getMonthlyDashboard(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                dashboardService.getMonthlyDashboard(
                        month,
                        year,
                        userId
                )
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                dashboardService.getSummary(userId)
        );
    }
}
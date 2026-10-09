package com.example.expensetracker.controller;

import com.example.expensetracker.dto.request.BudgetRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.BudgetResponse;
import com.example.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity.ok(
                budgetService.createBudget(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {

        return ResponseEntity.ok(
                budgetService.getAllBudgets()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                budgetService.getBudgetById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity.ok(
                budgetService.updateBudget(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBudget(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                budgetService.deleteBudget(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponse>> getBudgetsByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                budgetService.getBudgetsByUser(userId)
        );
    }

    @GetMapping("/month")
    public ResponseEntity<BudgetResponse> getBudgetByMonth(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                budgetService.getBudgetByMonth(month, year, userId)
        );
    }
}
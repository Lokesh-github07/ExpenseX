package com.example.expensetracker.controller;

import com.example.expensetracker.dto.request.ExpenseRequest;
import com.example.expensetracker.dto.response.ExpenseResponse;
import com.example.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.createExpense(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {

        return ResponseEntity.ok(
                expenseService.getAllExpenses()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                expenseService.getExpenseById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.updateExpense(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExpense(
            @PathVariable Long id) {

        expenseService.deleteExpense(id);

        return ResponseEntity.ok("Expense deleted successfully");
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                expenseService.getExpensesByCategory(categoryId)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseResponse>> getExpensesBetweenDates(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        return ResponseEntity.ok(
                expenseService.getExpensesBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpense() {

        return ResponseEntity.ok(
                expenseService.getTotalExpense()
        );
    }

    @GetMapping("/monthly-total")
    public ResponseEntity<BigDecimal> getMonthlyExpense(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                expenseService.getMonthlyExpense(month, year)
        );
    }
}
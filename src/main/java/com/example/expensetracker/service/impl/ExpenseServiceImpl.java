package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.ExpenseRequest;
import com.example.expensetracker.dto.response.ExpenseResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.CurrentUserService;
import com.example.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Every method here works ONLY on the authenticated user's expenses.
 * The user is taken from the SecurityContext (via CurrentUserService),
 * never from the request body or URL.
 *
 * Expenses that exist but belong to someone else are reported as
 * "not found" (404) so their existence is not revealed.
 */
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ExpenseResponse createExpense(
            ExpenseRequest request) {

        // If the client still sends userId it must be the caller's own;
        // otherwise 403. The expense is ALWAYS saved for the caller.
        Long userId =
                currentUserService.requireSelf(request.getUserId());

        Category category = findOwnedCategory(
                request.getCategoryId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .category(category)
                .user(user)
                .build();

        Expense savedExpense =
                expenseRepository.save(expense);

        return convertToResponse(savedExpense);
    }

    @Override
    public List<ExpenseResponse> getAllExpenses() {

        Long userId = currentUserService.getCurrentUserId();

        return expenseRepository.findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {

        return convertToResponse(findOwnedExpense(id));
    }

    @Override
    public ExpenseResponse updateExpense(
            Long id,
            ExpenseRequest request) {

        // Ownership check first: someone else's expense -> 404
        Expense expense = findOwnedExpense(id);

        // The owner of an expense can never be changed through update.
        Category category = findOwnedCategory(
                request.getCategoryId(),
                currentUserService.getCurrentUserId());

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);

        Expense updatedExpense =
                expenseRepository.save(expense);

        return convertToResponse(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {

        Expense expense = findOwnedExpense(id);

        expenseRepository.delete(expense);
    }

    @Override
    public List<ExpenseResponse> getExpensesByCategory(
            Long categoryId) {

        Long userId = currentUserService.getCurrentUserId();

        return expenseRepository
                .findByUserIdAndCategoryIdOrderByIdAsc(
                        userId,
                        categoryId
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<ExpenseResponse> getExpensesBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {

        Long userId = currentUserService.getCurrentUserId();

        return expenseRepository
                .findByUserIdAndExpenseDateBetweenOrderByIdAsc(
                        userId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BigDecimal getTotalExpense() {

        return expenseRepository.getTotalExpenseByUser(
                currentUserService.getCurrentUserId()
        );
    }

    @Override
    public BigDecimal getMonthlyExpense(
            int month,
            int year) {

        return expenseRepository
                .getMonthlyExpenseByUser(
                        currentUserService.getCurrentUserId(),
                        month,
                        year
                );
    }

    // ------------------------------------------------------------
    // Ownership helpers
    // ------------------------------------------------------------

    private Expense findOwnedExpense(Long id) {

        return expenseRepository
                .findByIdAndUserId(
                        id,
                        currentUserService.getCurrentUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"));
    }

    private Category findOwnedCategory(
            Long categoryId,
            Long userId) {

        return categoryRepository
                .findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));
    }

    private ExpenseResponse convertToResponse(
            Expense expense) {

        ExpenseResponse response =
                modelMapper.map(
                        expense,
                        ExpenseResponse.class
                );

        response.setCategoryId(
                expense.getCategory().getId()
        );

        response.setCategoryName(
                expense.getCategory().getName()
        );

        response.setUserId(
                expense.getUser().getId()
        );

        return response;
    }
}

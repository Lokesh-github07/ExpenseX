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
import com.example.expensetracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ExpenseResponse createExpense(
            ExpenseRequest request) {

        Category category = categoryRepository.findById(
                        request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

        User user = userRepository.findById(
                        request.getUserId())
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

        return expenseRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"));

        return convertToResponse(expense);
    }

    @Override
    public ExpenseResponse updateExpense(
            Long id,
            ExpenseRequest request) {

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"));

        Category category = categoryRepository.findById(
                        request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"));

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

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"));

        expenseRepository.delete(expense);
    }

    @Override
    public List<ExpenseResponse> getExpensesByCategory(
            Long categoryId) {

        return expenseRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<ExpenseResponse> getExpensesBetweenDates(
            LocalDate startDate,
            LocalDate endDate) {

        return expenseRepository
                .findByExpenseDateBetween(
                        startDate,
                        endDate
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BigDecimal getTotalExpense() {

        return expenseRepository.getTotalExpense();
    }

    @Override
    public BigDecimal getMonthlyExpense(
            int month,
            int year) {

        return expenseRepository
                .getMonthlyExpense(
                        month,
                        year
                );
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
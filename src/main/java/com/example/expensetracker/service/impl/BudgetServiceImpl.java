package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.BudgetRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.BudgetResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.CurrentUserService;
import com.example.expensetracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CurrentUserService currentUserService;

    @Override
    public BudgetResponse createBudget(BudgetRequest request) {
        // Budget is always created for the authenticated user.
        Long userId = currentUserService.requireSelf(request.getUserId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .user(user)
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        return convertToResponse(savedBudget);
    }

    @Override
    public List<BudgetResponse> getAllBudgets() {
        return budgetRepository
                .findByUserId(currentUserService.getCurrentUserId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = findOwnedBudget(id);

        return convertToResponse(budget);
    }

    @Override
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = findOwnedBudget(id);

        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToResponse(updatedBudget);
    }

    @Override
    public ApiResponse deleteBudget(Long id) {
        Budget budget = findOwnedBudget(id);

        budgetRepository.delete(budget);

        return new ApiResponse(true, "Budget deleted successfully");
    }

    @Override
    public List<BudgetResponse> getBudgetsByUser(Long userId) {
        currentUserService.requireSelf(userId);

        return budgetRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BudgetResponse getBudgetByMonth(Integer month, Integer year, Long userId) {
        currentUserService.requireSelf(userId);

        Budget budget = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return convertToResponse(budget);
    }

    private Budget findOwnedBudget(Long id) {
        return budgetRepository
                .findByIdAndUserId(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = modelMapper.map(budget, BudgetResponse.class);
        response.setUserId(budget.getUser().getId());
        return response;
    }
}
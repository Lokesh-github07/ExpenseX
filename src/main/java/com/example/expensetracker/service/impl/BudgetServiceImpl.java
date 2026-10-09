package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.BudgetRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.BudgetResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.UserRepository;
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

    @Override
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = userRepository.findById(request.getUserId())
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
        return budgetRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BudgetResponse getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return convertToResponse(budget);
    }

    @Override
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToResponse(updatedBudget);
    }

    @Override
    public ApiResponse deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        budgetRepository.delete(budget);

        return new ApiResponse(true, "Budget deleted successfully");
    }

    @Override
    public List<BudgetResponse> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public BudgetResponse getBudgetByMonth(Integer month, Integer year, Long userId) {
        Budget budget = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return convertToResponse(budget);
    }

    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = modelMapper.map(budget, BudgetResponse.class);
        response.setUserId(budget.getUser().getId());
        return response;
    }
}
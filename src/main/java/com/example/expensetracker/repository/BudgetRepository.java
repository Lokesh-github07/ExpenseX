package com.example.expensetracker.repository;

import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndMonthAndYear(
            Long userId,
            Integer month,
            Integer year
    );

    Optional<Budget> findByUserAndMonthAndYear(
            User user,
            int month,
            int year
    );

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);
}
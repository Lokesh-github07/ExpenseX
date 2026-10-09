package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);

    Page<Expense> findByUser(User user, Pageable pageable);

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findByExpenseDateBetween(
            LocalDate start,
            LocalDate end
    );

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e")
    BigDecimal getTotalExpense();

    @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE MONTH(e.expenseDate)=:month
            AND YEAR(e.expenseDate)=:year
            """)
    BigDecimal getMonthlyExpense(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    long countByUserId(Long userId);

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);

    @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.user.id=:userId
            """)
    BigDecimal getTotalExpenseByUser(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.user.id=:userId
            AND MONTH(e.expenseDate)=:month
            AND YEAR(e.expenseDate)=:year
            """)
    BigDecimal getMonthlyExpenseByUser(
            @Param("userId") Long userId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("""
            SELECT c.name, COALESCE(SUM(e.amount),0)
            FROM Expense e
            JOIN e.category c
            WHERE e.user.id = :userId
            GROUP BY c.name
            ORDER BY SUM(e.amount) DESC
            """)
    List<Object[]> getCategoryWiseExpenseByUser(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT YEAR(e.expenseDate),
                   MONTH(e.expenseDate),
                   COALESCE(SUM(e.amount),0)
            FROM Expense e
            WHERE e.user.id = :userId
            AND e.expenseDate >= :startDate
            GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
            ORDER BY YEAR(e.expenseDate), MONTH(e.expenseDate)
            """)
    List<Object[]> getMonthlyTrendByUser(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateAsc(
            Long userId,
            LocalDate start,
            LocalDate end
    );
}
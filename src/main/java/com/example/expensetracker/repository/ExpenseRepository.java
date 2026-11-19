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
import java.util.Optional;

/**
 * SECURITY NOTE: every query in this repository is scoped to a single user.
 * Do NOT add unscoped queries (findAll, SUM over the whole table, finders by
 * category or date alone). Expenses are per-user financial data.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);

    Page<Expense> findByUser(User user, Pageable pageable);

    // ---------- Owner-scoped lookups used by ExpenseService ----------

    List<Expense> findByUserIdOrderByIdAsc(Long userId);

    // Ownership check for get / update / delete: empty when the expense
    // does not exist OR belongs to someone else.
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserIdAndCategoryIdOrderByIdAsc(
            Long userId,
            Long categoryId
    );

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByIdAsc(
            Long userId,
            LocalDate start,
            LocalDate end
    );

    long countByUserId(Long userId);

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);

    // ---------- Owner-scoped aggregates ----------

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

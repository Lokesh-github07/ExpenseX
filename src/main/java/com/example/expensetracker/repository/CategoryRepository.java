package com.example.expensetracker.repository;

import com.example.expensetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Categories are per-user: names only need to be unique within one user.
    boolean existsByNameAndUserId(String name, Long userId);

    Optional<Category> findByNameAndUserId(
            String name,
            Long userId
    );

    // Owner-scoped lookups
    List<Category> findByUserIdOrderByIdAsc(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);
}

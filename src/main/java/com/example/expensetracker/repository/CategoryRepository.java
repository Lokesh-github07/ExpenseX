package com.example.expensetracker.repository;

import com.example.expensetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameAndUserId(
            String name,
            Long userId
    );

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);
}
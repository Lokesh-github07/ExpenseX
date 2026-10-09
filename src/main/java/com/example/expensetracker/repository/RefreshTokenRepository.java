package com.example.expensetracker.repository;

import com.example.expensetracker.model.RefreshToken;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);
}
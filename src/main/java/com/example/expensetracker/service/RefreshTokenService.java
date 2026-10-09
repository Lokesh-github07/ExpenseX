package com.example.expensetracker.service;

import com.example.expensetracker.model.RefreshToken;
import com.example.expensetracker.model.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(
            User user
    );

    Optional<RefreshToken> findByToken(
            String token
    );

    RefreshToken verifyExpiration(
            RefreshToken token
    );

    void deleteByUserId(
            Long userId
    );
}
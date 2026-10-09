package com.example.expensetracker.service.impl;

import com.example.expensetracker.exception.BadRequestException;
import com.example.expensetracker.model.RefreshToken;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.RefreshTokenRepository;
import com.example.expensetracker.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(
            User user) {

        // Remove any existing refresh token for this user first,
        // since the user_id column is unique (OneToOne) and a second
        // insert would otherwise violate that constraint on re-login.
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(
                                Instant.now()
                                        .plusMillis(refreshExpiration)
                        )
                        .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(
            String token) {

        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(
            RefreshToken token) {

        if (token.getExpiryDate()
                .compareTo(Instant.now()) < 0) {

            refreshTokenRepository.delete(token);

            throw new BadRequestException(
                    "Refresh token expired. Please login again."
            );
        }

        return token;
    }

    @Override
    public void deleteByUserId(Long userId) {

        refreshTokenRepository.deleteByUserId(userId);
    }
}
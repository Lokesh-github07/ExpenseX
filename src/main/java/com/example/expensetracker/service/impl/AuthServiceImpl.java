package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.EmailOnlyRequest;
import com.example.expensetracker.dto.request.ForgotPasswordRequest;
import com.example.expensetracker.dto.request.LoginRequest;
import com.example.expensetracker.dto.request.RegisterRequest;
import com.example.expensetracker.dto.request.ResetPasswordRequest;
import com.example.expensetracker.dto.request.VerifyOtpRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.AuthResponse;
import com.example.expensetracker.exception.BadRequestException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtService;
import com.example.expensetracker.security.UserPrincipal;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.EmailService;
import com.example.expensetracker.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Generates a 6-digit numeric OTP
    private String generateOtp() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private AuthResponse buildTokensForUser(User user) {

        UserPrincipal userPrincipal = new UserPrincipal(user);

        String accessToken =
                jwtService.generateToken(userPrincipal);

        String refreshToken =
                refreshTokenService
                        .createRefreshToken(user)
                        .getToken();

        return AuthResponse.builder()
                .otpRequired(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    public ApiResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "Email already registered"
            );
        }

        String otp = generateOtp();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.ROLE_USER)
                .active(true)
                .emailVerified(false)
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(10))
                .webhookToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);

        emailService.sendRegistrationOtpEmail(
                user.getEmail(),
                otp
        );

        return new ApiResponse(
                true,
                "Registered. Please check your email for the OTP to verify your account."
        );
    }

    @Override
    public ApiResponse verifyRegistrationOtp(VerifyOtpRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return new ApiResponse(true, "Email already verified");
        }

        if (user.getOtpCode() == null
                || !user.getOtpCode().equals(request.getOtp())) {

            throw new BadRequestException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null
                || user.getOtpExpiry().isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "OTP expired. Please request a new one."
            );
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Email verified successfully. You can now log in."
        );
    }

    @Override
    public ApiResponse resendRegistrationOtp(EmailOnlyRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return new ApiResponse(true, "Email already verified");
        }

        String otp = generateOtp();

        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        emailService.sendRegistrationOtpEmail(
                user.getEmail(),
                otp
        );

        return new ApiResponse(
                true,
                "OTP resent. Please check your email."
        );
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        User user = userRepository
                .findByEmail(userPrincipal.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Please verify your email first. Check your inbox for the OTP sent at registration."
            );
        }

        // Password is correct - send login OTP
        String otp = generateOtp();

        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendLoginOtpEmail(
                user.getEmail(),
                otp
        );

        return AuthResponse.builder()
                .otpRequired(true)
                .message(
                        "OTP sent to your email. Please verify to complete login."
                )
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }

    @Override
    public AuthResponse verifyLoginOtp(VerifyOtpRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (user.getOtpCode() == null
                || !user.getOtpCode().equals(request.getOtp())) {

            throw new BadRequestException("Invalid OTP");
        }

        if (user.getOtpExpiry() == null
                || user.getOtpExpiry().isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "OTP expired. Please log in again to receive a new one."
            );
        }

        user.setOtpCode(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return buildTokensForUser(user);
    }

    @Override
    public AuthResponse refreshToken(
            String refreshToken) {

        return refreshTokenService
                .findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {

                    User user = token.getUser();

                    UserPrincipal principal =
                            new UserPrincipal(user);

                    String accessToken =
                            jwtService.generateToken(
                                    principal
                            );

                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .userId(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .build();
                })
                .orElseThrow(() ->
                        new BadRequestException(
                                "Invalid refresh token"
                        ));
    }

    // ============================================================
    // FORGOT PASSWORD - SEND OTP
    // ============================================================

    @Override
    public ApiResponse forgotPassword(
            ForgotPasswordRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // Generate a new 6-digit password reset OTP
        String otp = generateOtp();

        // Save OTP and expiry separately from login/registration OTP
        user.setResetOtp(otp);
        user.setResetOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        userRepository.save(user);

        // Send OTP to user's email
        emailService.sendPasswordResetOtpEmail(
                user.getEmail(),
                otp
        );

        return new ApiResponse(
                true,
                "Password reset OTP sent to your email."
        );
    }

    // ============================================================
    // RESET PASSWORD - VERIFY OTP + CHANGE PASSWORD
    // ============================================================

    @Override
    public ApiResponse resetPassword(
            ResetPasswordRequest request) {

        // Check passwords
        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new BadRequestException(
                    "Passwords do not match"
            );
        }

        // Find user by email
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        // Check whether an OTP exists
        if (user.getResetOtp() == null) {

            throw new BadRequestException(
                    "No password reset OTP found. Please request a new OTP."
            );
        }

        // Check OTP
        if (!user.getResetOtp()
                .equals(request.getOtp())) {

            throw new BadRequestException(
                    "Invalid password reset OTP"
            );
        }

        // Check OTP expiry
        if (user.getResetOtpExpiry() == null
                || user.getResetOtpExpiry()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Password reset OTP expired. Please request a new OTP."
            );
        }

        // Change password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        // Invalidate OTP after successful reset
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);

        // Also invalidate old reset-token data
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Password reset successfully. You can now log in."
        );
    }
}
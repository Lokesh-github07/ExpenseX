package com.example.expensetracker.controller;

import com.example.expensetracker.dto.request.EmailOnlyRequest;
import com.example.expensetracker.dto.request.ForgotPasswordRequest;
import com.example.expensetracker.dto.request.LoginRequest;
import com.example.expensetracker.dto.request.RegisterRequest;
import com.example.expensetracker.dto.request.ResetPasswordRequest;
import com.example.expensetracker.dto.request.VerifyOtpRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.AuthResponse;
import com.example.expensetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.registerUser(request)
        );
    }

    // Step 2 of registration - confirm the OTP that was emailed
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyRegistrationOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyRegistrationOtp(request)
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendRegistrationOtp(
            @Valid @RequestBody EmailOnlyRequest request) {

        return ResponseEntity.ok(
                authService.resendRegistrationOtp(request)
        );
    }

    // Step 1 of login - validates password, emails an OTP, returns otpRequired:true
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.loginUser(request)
        );
    }

    // Step 2 of login - confirm the OTP, receive the real access/refresh tokens
    @PostMapping("/login/verify-otp")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyLoginOtp(request)
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestParam String refreshToken) {

        return ResponseEntity.ok(
                authService.refreshToken(refreshToken)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth Service Running");
    }

}
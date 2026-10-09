package com.example.expensetracker.service;

import com.example.expensetracker.dto.request.EmailOnlyRequest;
import com.example.expensetracker.dto.request.ForgotPasswordRequest;
import com.example.expensetracker.dto.request.LoginRequest;
import com.example.expensetracker.dto.request.RegisterRequest;
import com.example.expensetracker.dto.request.ResetPasswordRequest;
import com.example.expensetracker.dto.request.VerifyOtpRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.AuthResponse;

public interface AuthService {

    ApiResponse registerUser(RegisterRequest request);

    // Step 2 of registration: confirms the OTP emailed after registerUser()
    ApiResponse verifyRegistrationOtp(VerifyOtpRequest request);

    ApiResponse resendRegistrationOtp(EmailOnlyRequest request);

    // Step 1 of login: checks the password and emails an OTP instead of tokens
    AuthResponse loginUser(LoginRequest request);

    // Step 2 of login: confirms the OTP and issues real tokens
    AuthResponse verifyLoginOtp(VerifyOtpRequest request);

    AuthResponse refreshToken(String refreshToken);

    ApiResponse forgotPassword(ForgotPasswordRequest request);

    ApiResponse resetPassword(ResetPasswordRequest request);
}
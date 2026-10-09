package com.example.expensetracker.service;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String body
    );

    void sendPasswordResetEmail(
            String email,
            String resetToken
    );

    void sendRegistrationOtpEmail(
            String email,
            String otp
    );

    void sendLoginOtpEmail(
            String email,
            String otp
    );

    void sendPasswordResetOtpEmail(
            String email,
            String otp
    );
}
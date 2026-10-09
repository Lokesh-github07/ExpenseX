package com.example.expensetracker.service.impl;

import com.example.expensetracker.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // When false (default), emails just print to the console instead of being
    // sent - useful for local dev without SMTP credentials. Set MAIL_ENABLED=true
    // and provide MAIL_USERNAME / MAIL_APP_PASSWORD to send real emails.
    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(
            String to,
            String subject,
            String body) {

        if (!mailEnabled) {

            System.out.println("=================================");
            System.out.println("EMAIL DISABLED (DEV MODE)");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("=================================");

            return;
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {

            // Never let a broken mail server block registration/login -
            // just log it and let the OTP-in-logs fallback still be visible.
            log.error("Failed to send email to {}: {}", to, e.getMessage());

            System.out.println("=================================");
            System.out.println("EMAIL SEND FAILED - FALLING BACK TO CONSOLE");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("=================================");
        }
    }

    @Override
    public void sendPasswordResetEmail(
            String email,
            String resetToken) {

        String subject = "Expense Tracker - Password Reset";

        String body =
                "Your password reset token is:\n\n"
                        + resetToken
                        + "\n\nThis token will expire in 15 minutes.";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendRegistrationOtpEmail(
            String email,
            String otp) {

        String subject = "Expense Tracker - Verify Your Email";

        String body =
                "Your registration OTP is: " + otp
                        + "\n\nThis OTP will expire in 10 minutes."
                        + "\nEnter it on the registration page to activate your account.";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendLoginOtpEmail(
            String email,
            String otp) {

        String subject = "Expense Tracker - Your Login OTP";

        String body =
                "Your login OTP is: " + otp
                        + "\n\nThis OTP will expire in 5 minutes."
                        + "\nIf you didn't try to log in, you can ignore this email.";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendPasswordResetOtpEmail(
            String email,
            String otp) {

        String subject = "Expense Tracker - Password Reset OTP";

        String body =
                "Your password reset OTP is: " + otp
                        + "\n\nThis OTP will expire in 5 minutes."
                        + "\nIf you did not request a password reset, you can ignore this email.";

        sendEmail(email, subject, body);
    }
}
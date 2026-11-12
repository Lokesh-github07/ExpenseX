package com.example.expensetracker.service.impl;

import com.example.expensetracker.config.MailStartupCheck;
import com.example.expensetracker.exception.EmailDeliveryException;
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
    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    // DEV ONLY - if a real send fails, print the mail to the console instead of
    // failing the request. Default false: failures must be visible.
    @Value("${mail.console-fallback:false}")
    private boolean consoleFallback;

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

        // Fail clearly when real delivery is enabled but credentials were not loaded.
        // This avoids reporting a successful OTP operation when SMTP is not configured.
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new EmailDeliveryException(
                    "Email delivery is enabled, but MAIL_USERNAME is not configured.", null);
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress.trim());
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Email '{}' sent to {}", subject, to);

        } catch (Exception e) {

            // Log the real reason + a how-to-fix hint (never the OTP itself).
            log.error("Failed to send email to {}\n  Reason: {}\n  Fix: {}", to,
                    MailStartupCheck.rootCause(e),
                    MailStartupCheck.hint(e));

            if (consoleFallback) {
                System.out.println("=================================");
                System.out.println("EMAIL SEND FAILED - DEV CONSOLE FALLBACK");
                System.out.println("To: " + to);
                System.out.println("Subject: " + subject);
                System.out.println("Body: " + body);
                System.out.println("=================================");
                return;
            }

            // Tell the caller the truth instead of pretending the mail went out.
            throw new EmailDeliveryException(
                    "We could not send the email right now. "
                            + "Please try again in a moment or contact support.", e);
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
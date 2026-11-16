
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

    // Enable or disable actual email delivery
    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    // Development only - never enable in production
    @Value("${mail.console-fallback:false}")
    private boolean consoleFallback;

    // Verified sender email address from Brevo
    @Value("${app.mail.from:}")
    private String fromAddress;

    // SMTP connection details for diagnostic messages
    @Value("${spring.mail.host:smtp-relay.brevo.com}")
    private String smtpHost;

    @Value("${spring.mail.port:2525}")
    private int smtpPort;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {

        if (!mailEnabled) {
            log.warn(
                    "Email delivery disabled. Recipient: {}, Subject: {}",
                    to,
                    subject
            );
            return;
        }

        // Validate sender configuration
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new EmailDeliveryException(
                    "Email delivery is enabled, but MAIL_FROM is not configured.",
                    null
            );
        }

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromAddress.trim());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            // Send email through Brevo SMTP
            mailSender.send(message);

            log.info(
                    "Email '{}' successfully submitted to SMTP for {}",
                    subject,
                    to
            );

        } catch (Exception e) {

            // Log the actual SMTP error and troubleshooting hint
            log.error(
                    "Failed to send email to {}. Reason: {}. Fix: {}",
                    to,
                    MailStartupCheck.rootCause(e),
                    MailStartupCheck.hint(e, smtpHost, smtpPort)
            );

            // Development-only fallback
            if (consoleFallback) {

                log.warn(
                        "DEV CONSOLE FALLBACK ENABLED. Email content will be printed."
                );

                System.out.println("=================================");
                System.out.println("EMAIL SEND FAILED - DEV FALLBACK");
                System.out.println("To: " + to);
                System.out.println("Subject: " + subject);
                System.out.println("Body: " + body);
                System.out.println("=================================");

                return;
            }

            // Never report successful delivery when SMTP fails
            throw new EmailDeliveryException(
                    "We could not send the email right now. "
                            + "Please try again in a moment or contact support.",
                    e
            );
        }
    }

    // ============================================================
    // PASSWORD RESET EMAIL
    // ============================================================

    @Override
    public void sendPasswordResetEmail(
            String email,
            String resetToken
    ) {

        String subject = "ExpenseX - Password Reset";

        String body =
                "Your password reset token is:\n\n"
                        + resetToken
                        + "\n\nThis token will expire in 15 minutes.";

        sendEmail(email, subject, body);
    }

    // ============================================================
    // REGISTRATION OTP EMAIL
    // ============================================================

    @Override
    public void sendRegistrationOtpEmail(
            String email,
            String otp
    ) {

        String subject = "ExpenseX - Verify Your Email";

        String body =
                "Your registration OTP is: " + otp
                        + "\n\nThis OTP will expire in 10 minutes."
                        + "\nEnter it on the registration page to activate your account.";

        sendEmail(email, subject, body);
    }

    // ============================================================
    // LOGIN OTP EMAIL
    // ============================================================

    @Override
    public void sendLoginOtpEmail(
            String email,
            String otp
    ) {

        String subject = "ExpenseX - Your Login OTP";

        String body =
                "Your login OTP is: " + otp
                        + "\n\nThis OTP will expire in 5 minutes."
                        + "\nIf you didn't try to log in, you can ignore this email.";

        sendEmail(email, subject, body);
    }

    // ============================================================
    // PASSWORD RESET OTP EMAIL
    // ============================================================

    @Override
    public void sendPasswordResetOtpEmail(
            String email,
            String otp
    ) {

        String subject = "ExpenseX - Password Reset OTP";

        String body =
                "Your password reset OTP is: " + otp
                        + "\n\nThis OTP will expire in 5 minutes."
                        + "\nIf you did not request a password reset, you can ignore this email.";

        sendEmail(email, subject, body);
    }
}

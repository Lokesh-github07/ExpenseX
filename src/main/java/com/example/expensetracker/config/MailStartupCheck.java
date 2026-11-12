package com.example.expensetracker.config;

import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Performs a non-blocking SMTP login check after the application starts.
 * It never logs the SMTP password or its length.
 */
@Slf4j
@Component
public class MailStartupCheck {

    private final JavaMailSender mailSender;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    public MailStartupCheck(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        if (!mailEnabled) {
            log.info("Email delivery is disabled (MAIL_ENABLED=false). OTP messages use the configured development behavior.");
            return;
        }

        if (!(mailSender instanceof JavaMailSenderImpl sender)) {
            log.warn("SMTP startup check skipped: configured JavaMailSender is not JavaMailSenderImpl.");
            return;
        }

        String username = sender.getUsername();
        String password = sender.getPassword();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.error("MAIL CHECK FAILED: SMTP credentials are missing. Set MAIL_USERNAME and MAIL_APP_PASSWORD.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            String destination = sender.getHost() + ":" + sender.getPort();
            try {
                sender.testConnection();
                log.info("MAIL CHECK OK - SMTP authentication succeeded for {} at {}. OTP email delivery is ready.",
                        safeAddress(username), destination);
            } catch (Exception e) {
                log.error("MAIL CHECK FAILED for {} at {}. Reason: {}. Fix: {}",
                        safeAddress(username), destination, rootCause(e), hint(e));
            }
        });
    }

    private static String safeAddress(String address) {
        int at = address.indexOf('@');
        if (at <= 1) return "***";
        return address.substring(0, 1) + "***" + address.substring(at);
    }

    public static String rootCause(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable current = t;
        while (current != null) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append(current.getClass().getSimpleName());
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                // Do not print credentials if a provider unexpectedly includes them in an exception.
                sb.append(": ").append(message.replaceAll("(?i)(password|passcode)=?\\s*[^\\s,;]+", "$1=[REDACTED]"));
            }
            Throwable next = current.getCause();
            if (next == current) break;
            current = next;
        }
        return sb.toString();
    }

    public static String hint(Throwable t) {
        for (Throwable current = t; current != null; current = current.getCause()) {
            String message = String.valueOf(current.getMessage());
            if (current instanceof AuthenticationFailedException
                    || current instanceof MailAuthenticationException
                    || message.contains("535")
                    || message.contains("Username and Password not accepted")) {
                return "Gmail rejected SMTP authentication. Confirm 2-Step Verification is enabled, generate a NEW App Password for this exact Gmail account at https://myaccount.google.com/apppasswords, then update MAIL_APP_PASSWORD and restart the application. A code change cannot override Gmail's credential rejection.";
            }
            if (message.contains("PKIX") || message.contains("SSLHandshake") || message.contains("certificate")) {
                return "TLS certificate validation failed. Check antivirus email/SSL scanning, proxy, and system certificates.";
            }
            if (message.contains("timed out") || message.contains("Couldn't connect")
                    || message.contains("Connection refused") || message.contains("UnknownHost")) {
                return "Cannot reach smtp.gmail.com:587. Check internet, VPN, firewall, and whether the network blocks SMTP port 587.";
            }
        }
        return "Check SMTP host, port, TLS settings, and Gmail App Password.";
    }
}

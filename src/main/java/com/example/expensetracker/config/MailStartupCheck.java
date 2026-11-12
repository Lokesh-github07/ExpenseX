package com.example.expensetracker.config;

import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Tests the Gmail SMTP login once at startup and prints a clear verdict, so you
 * know immediately whether OTP emails can be delivered (and why not, if they can't).
 * Runs in the background - it never delays or blocks application startup.
 */
@Slf4j
@Component
public class MailStartupCheck {

    private final JavaMailSender mailSender;

    public MailStartupCheck(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        if (!(mailSender instanceof JavaMailSenderImpl sender)) {
            return;
        }
        new Thread(() -> {
            String who = sender.getUsername() + " @ " + sender.getHost() + ":" + sender.getPort()
                    + " (password length " + (sender.getPassword() == null ? 0 : sender.getPassword().length()) + ")";
            try {
                sender.testConnection();
                log.info("MAIL CHECK OK - SMTP login succeeded for {}. OTP emails will be delivered.", who);
            } catch (Exception e) {
                log.error("MAIL CHECK FAILED for {}\n  Reason: {}\n  Fix: {}", who, rootCause(e), hint(e));
            }
        }, "mail-startup-check").start();
    }

    public static String rootCause(Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable cur = t;
        while (cur != null) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append(cur.getClass().getSimpleName()).append(": ").append(cur.getMessage());
            cur = cur.getCause() == cur ? null : cur.getCause();
        }
        return sb.toString();
    }

    public static String hint(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String m = String.valueOf(c.getMessage());
            if (c instanceof AuthenticationFailedException || c instanceof MailAuthenticationException
                    || m.contains("535") || m.contains("Username and Password not accepted")) {
                return "Gmail rejected the login. Make sure 2-Step Verification is ON for the account, "
                        + "then create a NEW App Password (myaccount.google.com/apppasswords) and put it in MAIL_APP_PASSWORD.";
            }
            if (m.contains("PKIX") || m.contains("SSLHandshake") || m.contains("certificate")) {
                return "TLS was intercepted (antivirus 'mail shield' / proxy). Turn off email/SSL scanning in your antivirus.";
            }
            if (m.contains("timed out") || m.contains("Couldn't connect") || m.contains("Connection refused")
                    || m.contains("UnknownHost")) {
                return "Cannot reach smtp.gmail.com:587. Check internet/VPN/firewall; some networks block port 587.";
            }
        }
        return "See the reason above.";
    }
}

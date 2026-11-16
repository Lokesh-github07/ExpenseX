
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
            log.info(
                    "Email delivery disabled (MAIL_ENABLED=false)."
            );
            return;
        }

        if (!(mailSender instanceof JavaMailSenderImpl sender)) {
            log.warn(
                    "SMTP startup check skipped: unsupported JavaMailSender implementation."
            );
            return;
        }

        String username = sender.getUsername();
        String password = sender.getPassword();

        String host = sender.getHost();
        int port = sender.getPort();

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            log.error(
                    "MAIL CHECK FAILED: SMTP credentials are missing. "
                            + "Configure BREVO_SMTP_LOGIN and BREVO_SMTP_KEY."
            );
            return;
        }

        CompletableFuture.runAsync(() -> {

            String destination = host + ":" + port;

            try {

                sender.testConnection();

                log.info(
                        "MAIL CHECK OK - SMTP authentication succeeded for {} at {}. "
                                + "Email delivery connection is ready.",
                        safeAddress(username),
                        destination
                );

            } catch (Exception e) {

                log.error(
                        "MAIL CHECK FAILED for {} at {}. Reason: {}. Fix: {}",
                        safeAddress(username),
                        destination,
                        rootCause(e),
                        hint(e, host, port)
                );
            }
        });
    }

    private static String safeAddress(String address) {

        int at = address.indexOf('@');

        if (at <= 1) {
            return "***";
        }

        return address.substring(0, 1)
                + "***"
                + address.substring(at);
    }

    public static String rootCause(Throwable t) {

        StringBuilder sb = new StringBuilder();

        Throwable current = t;

        while (current != null) {

            if (sb.length() > 0) {
                sb.append(" -> ");
            }

            sb.append(current.getClass().getSimpleName());

            String message = current.getMessage();

            if (message != null && !message.isBlank()) {

                // Redact passwords and passcodes from exception messages.
                sb.append(": ").append(
                        message.replaceAll(
                                "(?i)(password|passcode)=?\\s*[^\\s,;]+",
                                "$1=[REDACTED]"
                        )
                );
            }

            Throwable next = current.getCause();

            if (next == current) {
                break;
            }

            current = next;
        }

        return sb.toString();
    }

    public static String hint(Throwable t, String host, int port) {

        for (Throwable current = t;
             current != null;
             current = current.getCause()) {

            String message = String.valueOf(current.getMessage());

            if (current instanceof AuthenticationFailedException
                    || current instanceof MailAuthenticationException
                    || message.contains("535")
                    || message.contains("Authentication failed")) {

                return "SMTP authentication failed. Verify your Brevo SMTP login "
                        + "and generated SMTP key. Make sure you are using the SMTP "
                        + "key, not the Brevo API key.";
            }

            if (message.contains("PKIX")
                    || message.contains("SSLHandshake")
                    || message.contains("certificate")) {

                return "TLS certificate validation failed. Check TLS configuration, "
                        + "system certificates, and proxy settings.";
            }

            if (message.contains("timed out")
                    || message.contains("Couldn't connect")
                    || message.contains("Connection refused")
                    || message.contains("UnknownHost")) {

                return "Cannot connect to " + host + ":" + port
                        + ". Verify the SMTP host and port. For Render Free, "
                        + "use Brevo SMTP port 2525 instead of blocked ports "
                        + "25, 465, or 587.";
            }
        }

        return "Check SMTP host, port, credentials, STARTTLS settings, "
                + "and your Brevo sender verification.";
    }
}

package com.example.expensetracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Normalizes SMTP credentials loaded from environment variables or .env.
 *
 * Gmail App Passwords are often copied with visual spaces. Windows-created
 * .env files can also contain carriage returns. Neither should be sent to SMTP.
 * This processor changes only the in-memory mail sender credentials.
 */
@Slf4j
@Configuration
public class MailConfig {

    @Bean
    public static BeanPostProcessor mailCredentialsSanitizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof JavaMailSenderImpl sender) {
                    String username = sender.getUsername();
                    if (username != null) {
                        sender.setUsername(username.trim());
                    }

                    String password = sender.getPassword();
                    if (password != null) {
                        // Google displays App Passwords in groups; whitespace is not part of it.
                        sender.setPassword(password.replaceAll("\\s+", ""));
                    }

                    String configuredUsername = sender.getUsername();
                    String configuredPassword = sender.getPassword();

                    if (configuredUsername == null || configuredUsername.isBlank()) {
                        log.warn("MAIL_USERNAME is missing. Configure the Gmail address used to send OTP emails.");
                    }
                    if (configuredPassword == null || configuredPassword.isBlank()) {
                        log.warn("MAIL_APP_PASSWORD is missing. Configure a Gmail App Password; never use the normal account password.");
                    } else if (configuredPassword.length() != 16) {
                        log.warn("MAIL_APP_PASSWORD has {} characters after whitespace cleanup. "
                                + "A Google App Password normally has 16 characters.", configuredPassword.length());
                    }
                }
                return bean;
            }
        };
    }
}

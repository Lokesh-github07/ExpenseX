package com.example.expensetracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Cleans the SMTP credentials before they are used.
 *
 * Why: a .env file saved with Windows (CRLF) line endings leaves a hidden
 * "\r" at the end of MAIL_USERNAME / MAIL_APP_PASSWORD. Gmail then rejects the
 * login, the mail is never sent, and the OTP only shows up in the console.
 * Gmail app passwords are also often pasted with spaces ("abcd efgh ijkl mnop").
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
                    if (sender.getUsername() != null) {
                        sender.setUsername(sender.getUsername().trim());
                    }
                    if (sender.getPassword() != null) {
                        // App passwords never contain whitespace
                        sender.setPassword(sender.getPassword().replaceAll("\\s+", ""));
                    }

                    String user = sender.getUsername();
                    if (user == null || user.isBlank() || user.startsWith("${")) {
                        log.warn("MAIL_USERNAME is not set - OTP emails cannot be sent. "
                                + "Define MAIL_USERNAME and MAIL_APP_PASSWORD in the run environment.");
                    }
                }
                return bean;
            }
        };
    }
}

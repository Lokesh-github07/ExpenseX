
package com.example.expensetracker.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Slf4j
@Configuration
public class MailConfig {

    @Bean
    public static BeanPostProcessor mailCredentialsSanitizer() {

        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(
                    Object bean,
                    String beanName) {

                if (bean instanceof JavaMailSenderImpl sender) {

                    // Normalize SMTP username
                    String username = sender.getUsername();

                    if (username != null) {
                        sender.setUsername(username.trim());
                    }

                    // Normalize SMTP password
                    // Remove accidental surrounding whitespace only.
                    // Do not remove internal characters from Brevo SMTP keys.
                    String password = sender.getPassword();

                    if (password != null) {
                        sender.setPassword(password.trim());
                    }

                    String configuredUsername = sender.getUsername();
                    String configuredPassword = sender.getPassword();

                    if (configuredUsername == null
                            || configuredUsername.isBlank()) {

                        log.warn(
                                "BREVO_SMTP_LOGIN is missing. "
                                        + "Configure your Brevo SMTP login."
                        );
                    }

                    if (configuredPassword == null
                            || configuredPassword.isBlank()) {

                        log.warn(
                                "BREVO_SMTP_KEY is missing. "
                                        + "Configure your generated Brevo SMTP key."
                        );
                    }

                    if (configuredUsername != null
                            && !configuredUsername.isBlank()
                            && configuredPassword != null
                            && !configuredPassword.isBlank()) {

                        log.info(
                                "SMTP credentials loaded successfully. "
                                        + "Credential values are hidden."
                        );
                    }
                }

                return bean;
            }
        };
    }
}

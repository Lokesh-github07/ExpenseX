package com.example.expensetracker.config;

import com.example.expensetracker.security.JwtAuthenticationEntryPoint;
import com.example.expensetracker.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CorsConfig corsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(
                        corsConfig.corsConfigurationSource()
                ))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public Pages
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/terms",
                                "/forgot-password",
                                "/reset-password",
                                "/dashboard",
                                "/add-expense",
                                "/edit-expense",
                                "/profile",
                                "/analytics",

                                // Static Resources
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**",

                                // Authentication APIs
                                "/api/auth/**",

                                // SMS webhook - authenticated via per-user token in the URL, not JWT
                                "/api/webhook/**",

                                // Swagger
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                // Misc
                                "/error"
                        ).permitAll()

                        // Everything else requires JWT
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
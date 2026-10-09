package com.example.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean active = true;

    // Old password reset token
    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    // Password reset OTP
    private String resetOtp;

    private LocalDateTime resetOtpExpiry;

    // Set to true once the user verifies the OTP sent at registration
    @Builder.Default
    private Boolean emailVerified = false;

    // Shared OTP slot - used for registration verification and login 2FA
    private String otpCode;

    private LocalDateTime otpExpiry;

    // Unguessable token used to authenticate the SMS-forwarding webhook
    // since a phone-side SMS forwarder can't hold a JWT.
    @Column(unique = true)
    private String webhookToken;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
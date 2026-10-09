package com.example.expensetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // When true, no tokens are issued yet - the client must call
    // /api/auth/login/verify-otp with the emailed OTP to receive real tokens.
    @Builder.Default
    private Boolean otpRequired = false;

    private String message;

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;
}
package com.example.expensetracker.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Boolean active;

    private Boolean emailVerified;

    private String webhookToken;

    private LocalDateTime createdAt;
}
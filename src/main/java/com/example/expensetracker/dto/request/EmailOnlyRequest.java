package com.example.expensetracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailOnlyRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;
}

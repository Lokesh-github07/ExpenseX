package com.example.expensetracker.controller;

import com.example.expensetracker.dto.request.SmsWebhookRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.SmsExpenseParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Receives bank transaction SMS forwarded from the user's phone (via an
 * Android SMS-forwarding app / automation) and auto-creates an expense.
 *
 * A real Spring Boot web app running in a browser cannot read a phone's SMS
 * inbox directly - only a native Android app with SMS permission can do that
 * (iOS doesn't allow it at all). This endpoint is the backend half of that
 * setup: point an SMS-forwarding automation at
 * POST /api/webhook/sms/{webhookToken} with a JSON body of
 * { "sender": "...", "message": "..." } and it will parse the message and
 * log an expense automatically.
 *
 * The webhookToken (see User.webhookToken) stands in for authentication here
 * since a phone-side automation generally can't attach a JWT bearer token.
 * Treat it like a password - it's returned once via GET /api/users/{id} and
 * should be kept private.
 */
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    private static final String AUTO_CATEGORY_NAME = "Bank Auto-Detected";

    @PostMapping("/sms/{webhookToken}")
    public ResponseEntity<ApiResponse> receiveSms(
            @PathVariable String webhookToken,
            @Valid @RequestBody SmsWebhookRequest request) {

        User user = userRepository
                .findByWebhookToken(webhookToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid webhook token"
                        ));

        SmsExpenseParser.ParsedTransaction parsed =
                SmsExpenseParser.parse(request.getMessage());

        if (parsed == null) {
            // Not a debit transaction we recognise (credit, OTP, promo, etc.)
            // - acknowledge receipt but don't create an expense.
            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "Message received but not logged (not a recognised debit transaction)"
                    )
            );
        }

        Category category = categoryRepository
                .findByNameAndUserId(AUTO_CATEGORY_NAME, user.getId())
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(AUTO_CATEGORY_NAME)
                                .description("Expenses auto-detected from bank SMS")
                                .user(user)
                                .build()
                ));

        Expense expense = Expense.builder()
                .title(parsed.getMerchant())
                .description(request.getMessage())
                .amount(parsed.getAmount())
                .expenseDate(LocalDate.now())
                .category(category)
                .user(user)
                .build();

        expenseRepository.save(expense);

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        "Expense of Rs. " + parsed.getAmount()
                                + " logged under '" + parsed.getMerchant() + "'"
                )
        );
    }
}

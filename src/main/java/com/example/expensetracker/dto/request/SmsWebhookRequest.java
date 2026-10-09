package com.example.expensetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload posted by an SMS-forwarding app (e.g. an Android "SMS Forwarder" /
 * Tasker / MacroDroid automation, or a small custom Android app using an
 * SMS BroadcastReceiver) whenever a new SMS arrives on the user's phone.
 */
@Data
public class SmsWebhookRequest {

    // The SMS sender ID, e.g. "HDFCBK", "VM-SBIINB" - optional, used for logging only
    private String sender;

    @NotBlank(message = "message is required")
    private String message;
}

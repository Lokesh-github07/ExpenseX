package com.example.expensetracker.exception;

/**
 * Thrown when an email (OTP, password reset...) could not be delivered by the
 * SMTP server. Mapped to HTTP 503 so the UI can tell the user the truth
 * instead of saying "OTP sent" when nothing was sent.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

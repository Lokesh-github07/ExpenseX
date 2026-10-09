package com.example.expensetracker.service;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very lightweight, regex-based parser for bank transaction SMS.
 *
 * Bank SMS formats vary a lot between banks (HDFC, SBI, ICICI, Axis, Kotak...),
 * so this is intentionally generic: it looks for an amount next to "Rs."/"INR"
 * and a debit-style keyword (debited/spent/paid/purchase) to decide whether the
 * message represents an expense worth logging. It is NOT meant to be 100%
 * accurate out of the box - expect to tune the patterns for your own bank's
 * exact wording once you see real messages coming through.
 *
 * Messages that don't look like a debit transaction (credits, OTPs, balance
 * enquiries, promotional SMS) are ignored and return null.
 */
public class SmsExpenseParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:Rs\\.?|INR)\\s?([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DEBIT_KEYWORDS = Pattern.compile(
            "\\b(debited|spent|paid|purchase|withdrawn|txn of)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CREDIT_KEYWORDS = Pattern.compile(
            "\\b(credited|refund|received|deposited)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Tries to pull a merchant/payee name out of common phrasings:
    // "...to VPA merchant@ok...", "...at AMAZON PAY...", "...to Pizza Hut..."
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "(?:to|at)\\s+([A-Za-z0-9@._\\- ]{3,40}?)(?:\\s+on|\\s+dt|\\.|,|$)",
            Pattern.CASE_INSENSITIVE
    );

    @Data
    @Builder
    public static class ParsedTransaction {
        private BigDecimal amount;
        private String merchant;
        private boolean debit;
    }

    /**
     * Returns null if the message doesn't look like a bank debit transaction
     * (e.g. it's a credit, an OTP message, or unrelated).
     */
    public static ParsedTransaction parse(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        boolean isDebit = DEBIT_KEYWORDS.matcher(message).find();
        boolean isCredit = CREDIT_KEYWORDS.matcher(message).find();

        // We only auto-log expenses, not incoming credits
        if (!isDebit || isCredit) {
            return null;
        }

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(message);

        if (!amountMatcher.find()) {
            return null;
        }

        String rawAmount = amountMatcher.group(1).replace(",", "");
        BigDecimal amount;

        try {
            amount = new BigDecimal(rawAmount);
        } catch (NumberFormatException e) {
            return null;
        }

        String merchant = "Bank Transaction";
        Matcher merchantMatcher = MERCHANT_PATTERN.matcher(message);

        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1).trim();
        }

        return ParsedTransaction.builder()
                .amount(amount)
                .merchant(merchant)
                .debit(true)
                .build();
    }
}

package com.example.expensetracker.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private DateUtil() {
    }

    public static final String DATE_PATTERN =
            "dd-MM-yyyy";

    public static final String DATE_TIME_PATTERN =
            "dd-MM-yyyy HH:mm:ss";

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    public static String formatDate(
            LocalDate date) {

        if (date == null) {
            return null;
        }

        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(
            LocalDateTime dateTime) {

        if (dateTime == null) {
            return null;
        }

        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static LocalDate parseDate(
            String date) {

        return LocalDate.parse(
                date,
                DATE_FORMATTER
        );
    }

    public static LocalDateTime parseDateTime(
            String dateTime) {

        return LocalDateTime.parse(
                dateTime,
                DATE_TIME_FORMATTER
        );
    }

    public static boolean isCurrentMonth(
            LocalDate date) {

        if (date == null) {
            return false;
        }

        YearMonth currentMonth =
                YearMonth.now();

        return YearMonth.from(date)
                .equals(currentMonth);
    }

    public static LocalDate getFirstDayOfMonth(
            int month,
            int year) {

        return LocalDate.of(
                year,
                month,
                1
        );
    }

    public static LocalDate getLastDayOfMonth(
            int month,
            int year) {

        return YearMonth.of(
                year,
                month
        ).atEndOfMonth();
    }
}
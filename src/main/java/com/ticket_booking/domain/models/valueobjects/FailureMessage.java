package com.ticket_booking.domain.models.valueobjects;

import java.util.Objects;

public record FailureMessage(String value) {

    private static final int MAX_LENGTH = 500;

    public FailureMessage {
        value = sanitize(value);
    }

    public static FailureMessage of(String value) {
        return value == null ? null : new FailureMessage(value);
    }

    private static String sanitize(String value) {
        Objects.requireNonNull(value, "failure message");

        String sanitized = value
                .replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        if (sanitized.length() > MAX_LENGTH) {
            sanitized = sanitized.substring(0, MAX_LENGTH);
        }

        return sanitized;
    }
}
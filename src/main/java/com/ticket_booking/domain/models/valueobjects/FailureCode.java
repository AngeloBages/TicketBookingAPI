package com.ticket_booking.domain.models.valueobjects;

import java.util.Objects;

import com.ticket_booking.payment.exceptions.InvalidPaymentFieldException;

public record FailureCode(String value) {

    private static final int MAX_LENGTH = 100;

    public FailureCode {
        value = sanitize(value);
    }

    public static FailureCode of(String value) {
        return value == null ? null : new FailureCode(value);
    }
    
    private static String sanitize(String value) {
    	Objects.requireNonNull(value, "failure code");

        value = value.trim();

        if (value.isEmpty()) {
            throw new InvalidPaymentFieldException(
                    "failureCode",
                    "must not be blank"
            );
        }

        if (value.length() > MAX_LENGTH) {
            value = value.substring(0, MAX_LENGTH);
        }

        if (!value.matches("[A-Za-z0-9_.-]+")) {
            throw new InvalidPaymentFieldException(
                    "failureCode",
                    "contains unsupported characters"
            );
        }
        
        return value;
    }
}

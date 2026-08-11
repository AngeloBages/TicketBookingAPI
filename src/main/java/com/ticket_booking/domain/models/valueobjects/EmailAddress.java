package com.ticket_booking.domain.models.valueobjects;

import java.util.Locale;

import com.ticket_booking.user.exceptions.InvalidUserFieldException;

public record EmailAddress(String value) {

    private static final int MAX_LENGTH = 255;

    public EmailAddress {
        if (value == null) {
            throw new InvalidUserFieldException(
                    "email",
                    "must not be null"
            );
        }

        value = value
                .trim()
                .toLowerCase(Locale.ROOT);

        if (value.isEmpty()) {
            throw new InvalidUserFieldException(
                    "email",
                    "must not be blank"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserFieldException(
                    "email",
                    "must have at most "
                            + MAX_LENGTH
                            + " characters"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}


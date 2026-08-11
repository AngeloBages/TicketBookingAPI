package com.ticket_booking.domain.models.valueobjects;

import com.ticket_booking.user.exceptions.InvalidUserFieldException;

public record UserName(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;

    public UserName {
        if (value == null) {
            throw new InvalidUserFieldException(
                    "name",
                    "must not be null"
            );
        }

        value = value
                .trim()
                .replaceAll("\\s+", " ");

        if (value.isEmpty()) {
            throw new InvalidUserFieldException(
                    "name",
                    "must not be blank"
            );
        }

        if (value.length() < MIN_LENGTH) {
            throw new InvalidUserFieldException(
                    "name",
                    "must have at least "
                            + MIN_LENGTH
                            + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserFieldException(
                    "name",
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


package com.ticket_booking.domain.models.valueobjects;

import com.ticket_booking.event.exceptions.InvalidEventFieldException;

public record EventDescription(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 500;

    public EventDescription {

        if (value == null) {
            throw new InvalidEventFieldException(
                    "description",
                    "must not be null"
            );
        }

        value = value.trim();

        if (value.isEmpty()) {
            throw new InvalidEventFieldException(
                    "description",
                    "must not be blank"
            );
        }

        if (value.length() < MIN_LENGTH) {
            throw new InvalidEventFieldException(
                    "description",
                    "must have at least "
                            + MIN_LENGTH
                            + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidEventFieldException(
                    "description",
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

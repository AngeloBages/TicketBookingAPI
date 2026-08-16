package com.ticket_booking.domain.models.valueobjects;

import com.ticket_booking.event.exceptions.InvalidEventFieldException;

public record EventTitle(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 60;

    public EventTitle {

        if (value == null) {
            throw new InvalidEventFieldException(
                    "title",
                    "must not be null"
            );
        }

        value = value
                .trim()
                .replaceAll("\\s+", " ");

        if (value.isEmpty()) {
            throw new InvalidEventFieldException(
                    "title",
                    "must not be blank"
            );
        }

        if (value.length() < MIN_LENGTH) {
            throw new InvalidEventFieldException(
                    "title",
                    "must have at least "
                            + MIN_LENGTH
                            + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidEventFieldException(
                    "title",
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

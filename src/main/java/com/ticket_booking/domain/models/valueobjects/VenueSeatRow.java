package com.ticket_booking.domain.models.valueobjects;

import java.util.Locale;

import com.ticket_booking.venue.exceptions.InvalidSeatFieldException;

public record VenueSeatRow(String value) {

	private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10;

    public VenueSeatRow {
        if (value == null) {
            throw new InvalidSeatFieldException(
                "row",
                "must not be null"
            );
        }

        value = value
            .trim()
            .replaceAll("\\s+", " ")
            .toUpperCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new InvalidSeatFieldException(
                "row",
                "must not be blank"
            );
        }
        
        if (value.length() < MIN_LENGTH) {
            throw new InvalidSeatFieldException(
                    "row",
                    "must have at least "
                            + MIN_LENGTH
                            + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidSeatFieldException(
                "row",
                "must have at most " + MAX_LENGTH + " characters"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

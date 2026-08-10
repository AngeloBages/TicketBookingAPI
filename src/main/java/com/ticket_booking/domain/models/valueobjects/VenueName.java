package com.ticket_booking.domain.models.valueobjects;

import com.ticket_booking.venue.exceptions.InvalidVenueFieldException;

public record VenueName(String value) {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    public VenueName {
        value = normalize(value);

        if (value.isEmpty()) {
            throw new InvalidVenueFieldException(
                    "name",
                    "must not be blank"
            );
        }

        if (value.length() < MIN_LENGTH) {
            throw new InvalidVenueFieldException(
                    "name",
                    "must have at least " + MIN_LENGTH + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidVenueFieldException(
                    "name",
                    "must have at most " + MAX_LENGTH + " characters"
            );
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new InvalidVenueFieldException(
                    "name",
                    "must not be null"
            );
        }

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }

    @Override
    public String toString() {
        return value;
    }
}

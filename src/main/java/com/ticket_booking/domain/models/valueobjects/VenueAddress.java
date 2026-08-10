package com.ticket_booking.domain.models.valueobjects;

import com.ticket_booking.venue.exceptions.InvalidVenueFieldException;

public record VenueAddress(String value) {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 255;

    public VenueAddress {
        value = normalize(value);

        if (value.isEmpty()) {
            throw new InvalidVenueFieldException(
                    "address",
                    "must not be blank"
            );
        }

        if (value.length() < MIN_LENGTH) {
            throw new InvalidVenueFieldException(
                    "address",
                    "must have at least " + MIN_LENGTH + " characters"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new InvalidVenueFieldException(
                    "address",
                    "must have at most " + MAX_LENGTH + " characters"
            );
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new InvalidVenueFieldException(
                    "address",
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

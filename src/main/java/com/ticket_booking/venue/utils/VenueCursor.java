package com.ticket_booking.venue.utils;

public record VenueCursor(
		Long id
) {
	
	public VenueCursor {
		if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "VenueCursor must have a valid venueId"
            );
        }
	}
}

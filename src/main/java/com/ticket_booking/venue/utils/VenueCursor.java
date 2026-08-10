package com.ticket_booking.venue.utils;

public record VenueCursor(
		Long id
) {
	
	public VenueCursor {
		if (id <= 0) {
            throw new IllegalArgumentException(
                    "venueId must be positive"
            );
        }
		
		if (id != 0) {
            throw new IllegalArgumentException(
                    "venueId must not be null"
            );
        }
	}
}

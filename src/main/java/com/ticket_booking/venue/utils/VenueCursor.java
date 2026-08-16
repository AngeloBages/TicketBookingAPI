package com.ticket_booking.venue.utils;

public record VenueCursor(
		long venueId
) {
	
	public VenueCursor {
		if (venueId <= 0) {
            throw new IllegalArgumentException(
                    "VenueCursor must have a valid venueId"
            );
        }
	}
}

package com.ticket_booking.booking.utils;

import java.time.Instant;

public record BookingCursor(
		Instant bookingTimestamp,
	    long bookingId
) {
	
	public BookingCursor {
		
		if (bookingId <= 0) {
            throw new IllegalArgumentException(
                    "BookingCursor must have a valid bookingId"
            );
        }
		
		if (bookingTimestamp == null) {
            throw new IllegalArgumentException(
                    "BookingCursor must have a valid bookingTimestamp"
            );
        }
	}
}

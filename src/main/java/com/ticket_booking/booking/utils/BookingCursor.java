package com.ticket_booking.booking.utils;

import java.time.LocalDateTime;

public record BookingCursor(
	    LocalDateTime bookingDate,
	    Long id
) {
	
	public BookingCursor {
		
		if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "BookingCursor must have a valid bookingId"
            );
        }
		
		if (bookingDate == null) {
            throw new IllegalArgumentException(
                    "BookingCursor must have a valid bookingDate"
            );
        }
	}
}

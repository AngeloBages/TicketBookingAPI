package com.ticket_booking.event.utils;

import java.time.LocalDate;

public record EventCursor(

		LocalDate eventDate,
		long eventId
) {
	
	public EventCursor {
		if(eventId == 0) {
			throw new IllegalArgumentException(
                    "EventCursor must have a valid eventId");
		}
		
		if(eventDate == null) {
			throw new IllegalArgumentException(
                    "EventCursor must have a valid eventDate");
		}
	}

}

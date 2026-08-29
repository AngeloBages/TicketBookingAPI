package com.ticket_booking.event.utils;

import java.time.LocalDateTime;

public record EventCursor(

		LocalDateTime eventDateTime,
		long eventId
) {
	
	public EventCursor {
		if(eventId == 0) {
			throw new IllegalArgumentException(
                    "EventCursor must have a valid eventId");
		}
		
		if(eventDateTime == null) {
			throw new IllegalArgumentException(
                    "EventCursor must have a valid eventDate");
		}
	}

}

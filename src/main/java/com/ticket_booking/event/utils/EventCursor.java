package com.ticket_booking.event.utils;

import java.time.Instant;

public record EventCursor(

		Instant eventDateTime,
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

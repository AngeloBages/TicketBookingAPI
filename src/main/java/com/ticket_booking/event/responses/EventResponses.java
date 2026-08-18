package com.ticket_booking.event.responses;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.EventSeatStatus;
import com.ticket_booking.domain.models.enums.EventStatus;


public final class EventResponses {
	
	private EventResponses() {}
	
	public record EventResponse(
			UUID id,
		    String title,
		    String description,
		    ZonedDateTime startsAt,
		    BigDecimal price,
		    EventStatus status,
		    VenueSummaryResponse venue,
		    List<EventSeatSummaryResponse> seats
	) {}

	public record EventSummaryResponse(
			UUID id,
		    String title,
		    String description,
		    ZonedDateTime startsAt,
		    BigDecimal price,
		    EventStatus status,
		    VenueSummaryResponse venue
	) {}
	
	public record VenueSummaryResponse(
			UUID id,
		    String name,
		    String address,
		    String timeZone
	) {}
	
	public record EventSeatSummaryResponse(
			UUID id,
	        int number,
	        String row,
	        EventSeatStatus status,
	        BigDecimal price
	) {}
}

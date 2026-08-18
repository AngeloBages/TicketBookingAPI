package com.ticket_booking.event.utils;


import com.ticket_booking.domain.models.Event;
import com.ticket_booking.event.responses.EventResponses.EventResponse;
import com.ticket_booking.event.responses.EventResponses.EventSummaryResponse;
import com.ticket_booking.event.responses.EventResponses.VenueSummaryResponse;
import com.ticket_booking.event.responses.EventResponses.EventSeatSummaryResponse;

public final class EventMapper {
	
	private EventMapper() {}
	
	public static EventSummaryResponse toSummaryResponse(Event event) {

	    return new EventSummaryResponse(
	            event.getUuid(),
	            event.getTitle(),
	            event.getDescription(),
	            event.getStartsAtAtVenue(),
	            event.getPrice(),
	            event.getStatus(),
	            
	            new VenueSummaryResponse(
	                    event.getVenue().getUuid(),
	                    event.getVenue().getName(),
	                    event.getVenue().getAddress(),
	                    event.getVenue().getTimeZone().getId()
	            )
	    );
	}
	
	public static EventResponse toResponse(Event event) {

	    return new EventResponse(
	            event.getUuid(),
	            event.getTitle(),
	            event.getDescription(),
	            event.getStartsAtAtVenue(),
	            event.getPrice(),
	            event.getStatus(),
	            
	            new VenueSummaryResponse(
	                    event.getVenue().getUuid(),
	                    event.getVenue().getName(),
	                    event.getVenue().getAddress(),
	                    event.getVenue().getTimeZone().getId()
	            ),
	            
	            event.getEventSeats()
	            	.stream()
	            	.map(eventSeat -> new EventSeatSummaryResponse(
	            			eventSeat.getUuid(),
	            			eventSeat.getSeat().getNumber(),
	            			eventSeat.getSeat().getRow(),
	            			eventSeat.getStatus(),
	            			eventSeat.getPrice()
	            	))
	            	.toList()
	    );
	}
}

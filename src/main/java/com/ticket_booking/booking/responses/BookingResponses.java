package com.ticket_booking.booking.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.BookingStatus;

public final class BookingResponses {
	
	private BookingResponses() {}
	
	public record BookingResponse(
		    UUID bookingId,
		    BookingStatus status,
		   Instant bookedAt,
		    BigDecimal totalPrice,
		    EventSummaryResponse event,
		    VenueSummaryResponse venue,
		    List<BookingSeatSummaryResponse> seats
	) {}
	
	public record EventSummaryResponse(
			UUID id,
		    String title,
		    String description,
		    ZonedDateTime startsAt,
		    BigDecimal price
	) {}
	
	public record VenueSummaryResponse(
			UUID id,
		    String name,
		    String address,
		    String timeZone
	) {}
	
	public record BookingSeatSummaryResponse(
	        int number,
	        String row,
	        BigDecimal price
	) {}
}

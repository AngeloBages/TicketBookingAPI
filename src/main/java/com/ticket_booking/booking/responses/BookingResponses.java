package com.ticket_booking.booking.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.BookingStatus;
import com.ticket_booking.domain.models.enums.EventSeatStatus;

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
		    LocalDate date,
		    BigDecimal price
	) {}
	
	public record VenueSummaryResponse(
			UUID id,
		    String name,
		    String address
	) {}
	
	public record BookingSeatSummaryResponse(
	        int number,
	        String row,
	        EventSeatStatus status,
	        BigDecimal price
	) {}
}

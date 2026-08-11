package com.ticket_booking.booking.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.BookingStatus;

public class BookingResponses {
	
	public record BookingResponse(
		    UUID bookingId,
		    BookingStatus status,
		   Instant bookedAt,
		    BigDecimal totalPrice,
		    EventSummaryResponse event,
		    VenueSummaryResponse venue,
		    List<SeatResponse> seats
	) {}
	
	public record EventSummaryResponse(
			UUID id,
		    String title,
		    LocalDate date,
		    BigDecimal price
	) {}
	
	public record VenueSummaryResponse(
			UUID id,
		    String name,
		    String address
	) {}
	
	public record SeatResponse(
		    String row,
		    int number
	) {}
}

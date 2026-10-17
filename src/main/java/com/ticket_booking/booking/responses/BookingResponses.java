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
		    ZonedDateTime endsAt,
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
	
	public enum BookingConfirmationResult {
		BOOKING_CONFIRMED,
		REFUND_REQUIRED;
		
		public boolean requiresRefund() {
			return this == REFUND_REQUIRED;
		}
	}
}

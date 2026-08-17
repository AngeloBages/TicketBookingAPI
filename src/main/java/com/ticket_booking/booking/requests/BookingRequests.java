package com.ticket_booking.booking.requests;

import java.util.List;
import java.util.UUID;

import com.ticket_booking.common.validation.UniqueElements;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class BookingRequests {

	private BookingRequests() {}
	
	public record CreateBookingRequest(
			@NotNull
			UUID eventId,
			
			@NotEmpty
			@UniqueElements
			List<@NotNull UUID> seatIds
	) {}
}

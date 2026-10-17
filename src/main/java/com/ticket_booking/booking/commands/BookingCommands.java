package com.ticket_booking.booking.commands;

import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.User;

public final class BookingCommands {

	private BookingCommands() {}
	
	public record CreateBookingCommand(
			User user,
			UUID eventId,
			List<UUID> seatIds,
			String idempotencyKey
	) {}
	
	public record PayBookingCommand(
			Long userId,
			UUID bookingId,
			String idempotencyKey
	) {}
	
	public record RefundBookingCommand(
			Long userId,
			UUID bookingId,
			String idempotencyKey
	) {}
}

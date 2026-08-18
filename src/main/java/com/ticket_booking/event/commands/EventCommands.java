package com.ticket_booking.event.commands;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class EventCommands {

	private EventCommands() {}
	
	public record CreateEventCommand(
			String title,
			String description,
			LocalDateTime startsAt,
			BigDecimal price,
			UUID venueId
	) {}
	
	public record UpdateEventCommand(
			UUID eventId,
			String title,
			String description,
			LocalDateTime startsAt,
			BigDecimal price
	) {}
}

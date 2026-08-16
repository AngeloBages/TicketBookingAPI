package com.ticket_booking.event.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class EventCommands {

	private EventCommands() {}
	
	public record CreateEventCommand(
			String title,
			String description,
			LocalDate date,
			BigDecimal price,
			UUID venueId
	) {}
	
	public record UpdateEventCommand(
			UUID eventId,
			String title,
			String description,
			LocalDate date,
			BigDecimal price
	) {}
}

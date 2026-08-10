package com.ticket_booking.venue.commands;

import java.util.UUID;

public final class VenueCommands {

	public record CreateVenueCommand(
			String name,
			String address
	) {}
	
	public record UpdateVenueCommand(
			UUID id,
			String name,
			String address
	) {}
}

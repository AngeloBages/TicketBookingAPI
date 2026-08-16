package com.ticket_booking.venue.commands;

import java.util.List;
import java.util.UUID;

public final class VenueCommands {

	private VenueCommands() {}
	
	public record CreateVenueCommand(
			String name,
			String address,
			List<SeatDto> seats
	) {}
	
	public record UpdateVenueCommand(
			UUID id,
			String name,
			String address
	) {}
	
	public record SeatDto(
			int number,
			String row
	) {}
}

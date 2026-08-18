package com.ticket_booking.venue.commands;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public final class VenueCommands {

	private VenueCommands() {}
	
	public record CreateVenueCommand(
			String name,
			String address,
			ZoneId timeZone,
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

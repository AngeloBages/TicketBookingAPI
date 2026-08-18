package com.ticket_booking.venue.responses;

import java.util.List;
import java.util.UUID;

public final class VenueResponses {
	
	private VenueResponses() {}
	
	public record VenueSummaryResponse(
			UUID id,
			String name,
			String address,
			String timeZone,
			boolean active
	){}

	public record VenueResponse(
			UUID id,
			String name,
			String address,
			String timeZone,
			List<SeatResponse> seats
	){}
	
	public record SeatResponse(
			UUID id,
			int number,
			String row,
			boolean active
	) {}
}

package com.ticket_booking.venue.responses;

import java.util.UUID;

public final class VenueResponses {

	public record VenueResponse(
			UUID id,
			String name,
			String address
	){}
}

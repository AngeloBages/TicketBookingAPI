package com.ticket_booking.venue.utils;

import com.ticket_booking.domain.models.Venue;
import com.ticket_booking.venue.responses.VenueResponses.SeatResponse;
import com.ticket_booking.venue.responses.VenueResponses.VenueResponse;
import com.ticket_booking.venue.responses.VenueResponses.VenueSummaryResponse;

public final class VenueMapper {
	
	private VenueMapper() {}

	public static VenueSummaryResponse toSummaryResponse(Venue venue) {
		return new VenueSummaryResponse(
				venue.getUuid(),
				venue.getName(),
				venue.getAddress(),
				venue.getTimeZone().getId(),
				venue.isActive()
				);
	}
	
	public static VenueResponse toResponse(Venue venue) {
		return new VenueResponse(
				venue.getUuid(),
				venue.getName(),
				venue.getAddress(),
				venue.getTimeZone().getId(),
				venue.getSeats()
					.stream()
					.map(seat -> new SeatResponse(
							seat.getUuid(),
							seat.getNumber(),
							seat.getRow(),
							seat.isActive()
					))
					.toList()
				);
	}
}

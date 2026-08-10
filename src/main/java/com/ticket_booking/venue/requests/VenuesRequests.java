package com.ticket_booking.venue.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VenuesRequests {

	public record RegisterVenueRequest(
			
			@NotBlank
		    @Size(min = 8, max = 64)
			String name,
			
			@NotBlank
		    @Size(min = 10, max = 255)
			String address
	) {}
	
	public record UpdateVenueRequest(
			
			@NotBlank
		    @Size(min = 8, max = 64)
			String name,
			
			@NotBlank
		    @Size(min = 10, max = 255)
			String address
	) {}
}

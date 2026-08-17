package com.ticket_booking.venue.requests;

import java.util.List;

import com.ticket_booking.common.validation.UniqueElements;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public final class VenueRequests {
	
	private VenueRequests() {}

	public record VenueCreateRequest(
			
			@NotBlank
		    @Size(min = 8, max = 64)
			String name,
			
			@NotBlank
		    @Size(min = 10, max = 255)
			String address,
			
			@NotEmpty
			@UniqueElements
			List<@Valid SeatRequestDto> seats
	) {}
	
	public record VenueUpdateRequest(
			
			@NotBlank
		    @Size(min = 8, max = 64)
			String name,
			
			@NotBlank
		    @Size(min = 10, max = 255)
			String address
	) {}
	
	public record SeatRequestDto(
			
			@Max(5000)
			@Min(1)
			int number,
			
			@NotBlank
			@Size(min = 1, max = 10)
			String row
	) {}
}

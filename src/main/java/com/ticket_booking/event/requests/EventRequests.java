package com.ticket_booking.event.requests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class EventRequests {
	
	private EventRequests() {}

	public record EventCreateRequest(
			@NotBlank
			@Size(min = 8, max = 60)
			String title,
			
			@NotBlank
			@Size(min = 8, max = 500)
			String description,

			@NotNull
			@Future
			LocalDate date,
			
			@NotNull
			@PositiveOrZero
            @DecimalMax(value = "99999999.99")
            @Digits(integer = 8, fraction = 2)
			BigDecimal price,
			
			@NotNull
			UUID venueId
	) {}
	
	public record EventUpdateRequest(
			@NotBlank
			@Size(min = 8, max = 60)
			String title,
			
			@NotBlank
			@Size(min = 8, max = 500)
			String description,

			@NotNull
			@Future
			LocalDate date,
			
			@NotNull
			@PositiveOrZero
            @DecimalMax(value = "99999999.99")
            @Digits(integer = 8, fraction = 2)
			BigDecimal price
	) {}
}

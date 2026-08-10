package com.ticket_booking.venue;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ticket_booking.common.CursorPage;
import com.ticket_booking.venue.commands.VenueCommands.CreateVenueCommand;
import com.ticket_booking.venue.commands.VenueCommands.UpdateVenueCommand;
import static com.ticket_booking.venue.requests.VenuesRequests.*;
import com.ticket_booking.venue.responses.VenueResponses.VenueResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("api/venues")
@Tag(name = "Venue", description = "Venue endpoints")
public class VenueController {

	private final VenueService venueService;
	private final Validator validator;
	
	public VenueController(
			VenueService venueService,
			Validator validator) {
		
		this.venueService = venueService;
		this.validator = validator;
	}
	
	@GetMapping
	@Operation(summary = "Get venues' info")
	public ResponseEntity<CursorPage<VenueResponse>> getVenues(
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

		return ResponseEntity.ok(
				venueService.getVenues(cursor, limit));
	}
	
	@GetMapping("{id}")
	@Operation(summary = "Get info of a specific venue")
	public ResponseEntity<VenueResponse> getVenue(@PathVariable("id") UUID venueId) {
		
		return ResponseEntity.ok(
				venueService.getVenue(venueId));
	}
	
	@PostMapping
	@Operation(summary = "Register a new Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> registerVenue(@RequestBody RegisterVenueRequest request) {
		validateRequest(request);
		
		UUID venueId = venueService.createVenue(
				new CreateVenueCommand(
						request.name(),
						request.address()
				)
			);
		
		URI location = ServletUriComponentsBuilder
		        .fromCurrentRequest()
		        .path("/{id}")
		        .buildAndExpand(venueId)
		        .toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@PutMapping("{id}")
	@Operation(summary = "Update a Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateVenue(
			@PathVariable("id") UUID venueId, 
			@RequestBody UpdateVenueRequest request) {
		validateRequest(request);
		
		venueService.updateVenue(
				new UpdateVenueCommand(
						venueId,
						request.name(),
						request.address()
				)
			);
		
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("{id}")
	@Operation(summary = "Delete a Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteVenue(@PathVariable("id") UUID venueId) {
		
		venueService.deleteVenue(venueId);
		
		return ResponseEntity.noContent().build();
	}
	
	private <T> void validateRequest(T request) {
		Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations); // Triggers  400 Bad Request handler
        }
	}
}

package com.ticket_booking.venue;

import java.net.URI;
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
import com.ticket_booking.venue.commands.VenueCommands.SeatDto;
import com.ticket_booking.venue.commands.VenueCommands.UpdateVenueCommand;
import static com.ticket_booking.venue.requests.VenueRequests.*;
import com.ticket_booking.venue.responses.VenueResponses.VenueResponse;
import com.ticket_booking.venue.responses.VenueResponses.VenueSummaryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/venues")
@Tag(name = "Venue", description = "Venue endpoints")
public class VenueController {

	private final VenueService venueService;
	
	public VenueController(
			VenueService venueService) {
		
		this.venueService = venueService;
	}
	
	@GetMapping
	@Operation(summary = "Get venues' info")
	public ResponseEntity<CursorPage<VenueSummaryResponse>> getVenues(
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {

		return ResponseEntity.ok(
				venueService.getVenues(
						cursor, 
						limit
					)
				);
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Get info of a specific venue")
	public ResponseEntity<VenueResponse> getVenue(@PathVariable("id") UUID venueId) {
		
		return ResponseEntity.ok(
				venueService.getVenue(
						venueId
					)
				);
	}
	
	@PostMapping
	@Operation(summary = "Register a new Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> registerVenue(
			@Valid @RequestBody VenueCreateRequest request) {
		
		UUID venueId = venueService.createVenue(
				new CreateVenueCommand(
						request.name(),
						request.address(),
						request.seats()
						    .stream()
							.map(seat -> new SeatDto(
										seat.number(),
										seat.row()
									))
							.toList()
				)
			);
		
		URI location = ServletUriComponentsBuilder
		        .fromCurrentRequest()
		        .path("/{id}")
		        .buildAndExpand(venueId)
		        .toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update a Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateVenue(
			@PathVariable("id") UUID venueId, 
			@Valid @RequestBody VenueUpdateRequest request) {
		
		venueService.updateVenue(
				new UpdateVenueCommand(
						venueId,
						request.name(),
						request.address()
				)
			);
		
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/{id}")
	@Operation(summary = "Deactivate a Venue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deactivateVenue(@PathVariable("id") UUID venueId) {
		
		venueService.deactivateVenue(venueId);
		
		return ResponseEntity.noContent().build();
	}
}

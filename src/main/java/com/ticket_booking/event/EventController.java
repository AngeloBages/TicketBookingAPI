package com.ticket_booking.event;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ticket_booking.common.CursorPage;
import com.ticket_booking.event.commands.EventCommands.CreateEventCommand;
import com.ticket_booking.event.commands.EventCommands.UpdateEventCommand;
import com.ticket_booking.event.requests.EventRequests.EventCreateRequest;
import com.ticket_booking.event.requests.EventRequests.EventUpdateRequest;
import com.ticket_booking.event.responses.EventResponses.EventResponse;
import com.ticket_booking.event.responses.EventResponses.EventSeatSummaryResponse;
import com.ticket_booking.event.responses.EventResponses.EventSummaryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event", description = "Event endpoints")
public class EventController {
	
	private final EventService eventService;

	public EventController(
			EventService eventService) {
		
		this.eventService = eventService;
	}
	
	@GetMapping
	@Operation(summary = "Get events' info")
	public ResponseEntity<CursorPage<EventSummaryResponse>> getEvents(
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
		
		return ResponseEntity.ok(
				eventService.getEvents(
						cursor, 
						limit
				)
			);
	}
	
	@GetMapping("/{id}")
	@Operation(summary = "Get a specific event info")
	public ResponseEntity<EventResponse> getEvent(@PathVariable("id") UUID eventId) {
		
		return ResponseEntity.ok(
				eventService.getEvent(eventId)
			);
	}
	
	@GetMapping("/{id}/seats")
	@Operation(summary = "Get info of all seats of an event")
	public ResponseEntity<List<EventSeatSummaryResponse>> getEventSeats(@PathVariable("id") UUID eventId) {
		
		return ResponseEntity.ok(
				eventService.getEventSeats(eventId)
			);
	}
	
	@PostMapping
	@Operation(summary = "Create a new Event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> createEvent(
			@Valid @RequestBody EventCreateRequest request) {
		
		UUID eventId = eventService.createEvent(
				new CreateEventCommand(
						request.title(),
						request.description(),
						request.date(),
						request.price(),
						request.venueId()
				)
			);
		
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
		        .buildAndExpand(eventId)
		        .toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@PutMapping("/{id}")
	@Operation(summary = "Update an Event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> updateEvent(
			@PathVariable("id") UUID eventId,
			@Valid @RequestBody EventUpdateRequest request) {
		
		eventService.updateEvent(
				new UpdateEventCommand(
						eventId,
						request.title(),
						request.description(),
						request.date(),
						request.price()
				)
			);
		
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("/{id}/cancellation")
	@Operation(summary = "Cancel an Event")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteEvent(@PathVariable("id") UUID eventId) {
		
		eventService.cancelEvent(eventId);
		
		return ResponseEntity.noContent().build();
	}
}

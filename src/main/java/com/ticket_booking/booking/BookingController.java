package com.ticket_booking.booking;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ticket_booking.booking.commands.BookingCommands.CreateBookingCommand;
import com.ticket_booking.booking.commands.BookingCommands.PayBookingCommand;
import com.ticket_booking.booking.commands.BookingCommands.RefundBookingCommand;
import com.ticket_booking.booking.requests.BookingRequests.CreateBookingRequest;
import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.responses.BookingResponses.PayBookingResponse;
import com.ticket_booking.booking.responses.BookingResponses.RefundBookingResponse;
import com.ticket_booking.common.CursorPage;
import com.ticket_booking.common.security.AppUser;
import com.ticket_booking.common.web.HttpHeaders;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking", description = "Booking endpoints")
public class BookingController {

	private final BookingService bookingService;
	
	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}
	
	@PostMapping
	@Operation(summary = "Create a Booking")
	public ResponseEntity<Void> createBooking(
			@AuthenticationPrincipal AppUser appUser,
			
			@NotBlank @Size(max = 255) 
			@RequestHeader(HttpHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
			
			@Valid @RequestBody CreateBookingRequest request) {
		
		UUID bookingId = bookingService.createBooking(
				new CreateBookingCommand(
						appUser.getUser(),
						request.eventId(),
						request.seatIds(),
						idempotencyKey
				)
			);
		
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(bookingId)
				.toUri();
		
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping("{id}")
	@Operation(summary = "Get a specific logged-in user's booking's info")
	public ResponseEntity<BookingResponse> getUserBooking(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@PathVariable("id") UUID bookingId) {
		
		return ResponseEntity.ok(
				bookingService.getUserBooking(
						userId, 
						bookingId
					)
				);
	}

	@GetMapping
	@Operation(summary = "Get logged-in user's bookings' details")
	public ResponseEntity<CursorPage<BookingResponse>> getUserBookings(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
		
		return ResponseEntity.ok(
				bookingService.getUserBookings(
						userId,
						cursor,
						limit
					)
				);
	}
	
	@PatchMapping("/{id}/cancellation")
	public ResponseEntity<Void> cancelBooking(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@PathVariable("id") UUID bookingId){
		
		bookingService.cancelBooking(userId, bookingId);
		
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{id}/pay")
	@Operation(summary = "Creates a payment for a booking")
	public PayBookingResponse createPayment(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			
			@NotBlank @Size(max = 255) 
			@RequestHeader(HttpHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
			
			@PathVariable UUID id){

		return bookingService.payForBooking(
				new PayBookingCommand(
					userId,
					id, 
					idempotencyKey
				)
		);
	}
	
	@PostMapping("/{id}/refund")
	@Operation(summary = "Requests a refund for a booking")
	public ResponseEntity<RefundBookingResponse> requestRefund(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			
			@NotBlank @Size(max = 255) 
			@RequestHeader(HttpHeaders.IDEMPOTENCY_KEY) String idempotencyKey,
			
			@PathVariable UUID id){
		
		var response = bookingService.requestBookingRefund(
				new RefundBookingCommand(
						userId,
						id, 
						idempotencyKey
					)
				);

		return ResponseEntity.accepted().body(response);
	}
}

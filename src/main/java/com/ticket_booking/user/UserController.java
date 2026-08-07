package com.ticket_booking.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.booking.BookingService;
import com.ticket_booking.booking.dtos.BookingDtos.BookingResponse;
import com.ticket_booking.common.CursorPage;
import com.ticket_booking.common.security.AppUser;
import com.ticket_booking.user.commands.UserCommands.ChangePasswordCommand;
import com.ticket_booking.user.commands.UserCommands.UpdateUserCommand;
import com.ticket_booking.user.responses.UserResponses.UserInfoResponse;
import static com.ticket_booking.user.requests.UserRequests.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("api/users")
@Tag(name = "User", description = "User specifc endpoints")
public class UserController {
	
	private final UserService userService;
	private final BookingService bookingService;
	
	public UserController(
			UserService userService,
			BookingService bookingService) {
		this.userService = userService;
		this.bookingService = bookingService;
	}

	@GetMapping("me")
	@Operation(summary = "Get authenticated user's info")
	public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal AppUser appUser) {
		
		return ResponseEntity.ok(
				userService.getCurrentUser(appUser.getUser().getId()));
	}
	
	@PutMapping("me")
	@Operation(summary = "Update authenticated user's info")
	public ResponseEntity<Void> updateUserInfo(
			@AuthenticationPrincipal AppUser appUser,
			@Valid @RequestBody UpdateUserRequest request) {
		
		userService.updateUserInfo(new UpdateUserCommand(
				appUser.getUser().getId(),
				request.name(),
				request.email()
		));
		
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("me/password")
	@Operation(
		    summary = "Change password",
		    description = "Changes the authenticated user's password and revokes all active refresh tokens."
		)
	public ResponseEntity<Void> changePassword(
			@AuthenticationPrincipal AppUser appUser,
			@Valid @RequestBody ChangePasswordRequest request) {
		
		userService.changePassword(new ChangePasswordCommand(
				appUser.getUser().getId(),
		        request.currentPassword(),
		        request.newPassword(),
		        request.confirmPassword()
		));
		
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("me")
	@Operation(
			summary = "Delete user",
			description = "Deletes the authenticated user's account and revokes all active refresh tokens.")
	public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AppUser appUser) {

		userService.deleteUser(appUser.getUser().getId());
		
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("me/bookings")
	@Operation(summary = "Get authenticated user's bookings' details")
	public ResponseEntity<CursorPage<BookingResponse>> getUserBookings(
			@AuthenticationPrincipal AppUser appUser,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
		
		return ResponseEntity.ok(
				bookingService.getUserBookings(
						appUser.getUser().getId(),
						cursor,
						limit));
	}
}

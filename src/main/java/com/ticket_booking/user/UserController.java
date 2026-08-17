package com.ticket_booking.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.user.commands.UserCommands.ChangePasswordCommand;
import com.ticket_booking.user.commands.UserCommands.UpdateUserCommand;
import com.ticket_booking.user.responses.UserResponses.UserInfoResponse;
import static com.ticket_booking.user.requests.UserRequests.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User specifc endpoints")
public class UserController {
	
	private final UserService userService;
	
	public UserController(
			UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	@Operation(summary = "Get authenticated user's info")
	public ResponseEntity<UserInfoResponse> getUserInfo(
			@AuthenticationPrincipal(expression = "user.id") Long userId) {
		
		return ResponseEntity.ok(
				userService.getCurrentUser(
						userId
					)
				);
	}
	
	@PutMapping("/me")
	@Operation(summary = "Update authenticated user's info")
	public ResponseEntity<Void> updateUserInfo(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@Valid @RequestBody UpdateUserRequest request) {
		
		userService.updateUserInfo(
				new UpdateUserCommand(
					userId,
					request.name(),
					request.email()
				)
			);
		
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping("/me/password")
	@Operation(
		    summary = "Change password",
		    description = "Changes the authenticated user's password and revokes all active refresh tokens."
		)
	public ResponseEntity<Void> changePassword(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@Valid @RequestBody ChangePasswordRequest request) {
		
		userService.changePassword(
				new ChangePasswordCommand(
					userId,
			        request.currentPassword(),
			        request.newPassword(),
			        request.confirmPassword()
				)
			);
		
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/me")
	@Operation(
			summary = "Delete user",
			description = "Deletes the authenticated user's account and revokes all active refresh tokens.")
	public ResponseEntity<Void> deleteUser(
			@AuthenticationPrincipal(expression = "user.id") Long userId) {

		userService.deleteUser(userId);
		
		return ResponseEntity.noContent().build();
	}
}

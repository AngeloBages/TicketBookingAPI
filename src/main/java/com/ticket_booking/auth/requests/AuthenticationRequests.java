package com.ticket_booking.auth.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthenticationRequests {
	
	private AuthenticationRequests() {}
	
	public record RegisterUserRequest(
		@NotBlank String name,
		@NotBlank(message = "Email cannot be blank")
		@Email(message = "Invalid email format")
		String email,

		@NotBlank(message = "Password cannot be blank")
		String password
	) {}
	
	public record AuthenticateUserRequest(
		@NotBlank(message = "Email cannot be blank")
		@Email(message = "Invalid email format")
		String email,

		@NotBlank(message = "Password cannot be blank")
		String password
	) {}

	public record RefreshTokenRequest(
		@NotBlank(message = "Refresh token cannot be blank") String refreshToken
	) {}

	public record LogoutRequest(
		@NotBlank(message = "Refresh token cannot be blank") String refreshToken
	) { }
}
package com.ticket_booking.auth.responses;

public class AuthenticationResponses {

	public record AuthResponse(
			String accessToken,
			String refreshToken
	) {}
}

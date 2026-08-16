package com.ticket_booking.auth.responses;

public final class AuthenticationResponses {

	private AuthenticationResponses() {}
	
	public record AuthResponse(
			String accessToken,
			String refreshToken
	) {}
}

package com.ticket_booking.common.security.responses;

public class AuthenticationResponses {

	public record AuthResponse(
			String accessToken,
			String refreshToken
	) {}
}

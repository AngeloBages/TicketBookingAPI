package com.ticket_booking.auth.responses;

import com.ticket_booking.domain.models.RefreshToken;

public final class AuthenticationResponses {

	private AuthenticationResponses() {}
	
	public record AuthResponse(
			String accessToken,
			String refreshToken
	) {}
	
	public record RefreshTokenResult(
	        RefreshToken token,
	        String rawToken
	) {}
}

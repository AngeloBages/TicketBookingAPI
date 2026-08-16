package com.ticket_booking.user.responses;

import java.util.Set;
import java.util.UUID;

public final class UserResponses {

	private UserResponses() {}
	
	public record UserInfoResponse(
			UUID uuid,
			String name,
			String email,
			Set<String> roles
	) {}
}

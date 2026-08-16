package com.ticket_booking.admin.responses;

import java.util.Set;
import java.util.UUID;

public final class AdminResponses {

	private AdminResponses() {}
	
	public record UserInfoResponse(
			UUID uuid,
			String name,
			String email,
			Set<String> roles
	) {}
}

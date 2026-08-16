package com.ticket_booking.admin.requests;

import com.ticket_booking.common.AppRole;

import jakarta.validation.constraints.NotNull;

public final class AdminRequests {

	private AdminRequests() {}
	
	public record AssignRolesRequest(
			
			@NotNull
			AppRole roleName
	) {}
}

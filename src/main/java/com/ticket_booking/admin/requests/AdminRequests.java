package com.ticket_booking.admin.requests;

import com.ticket_booking.common.AppRole;

import jakarta.validation.constraints.NotNull;

public class AdminRequests {

	public record AssignRolesRequest(
			
			@NotNull
			AppRole roleName
	) {}
}

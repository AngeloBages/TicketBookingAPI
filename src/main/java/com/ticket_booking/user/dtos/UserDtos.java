package com.ticket_booking.user.dtos;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {
	
	public record UpdateUserRequest(
		    @NotBlank
		    @Size(min = 3, max = 120)
		    String name,

		    @NotBlank
		    @Email
		    @Size(max = 255)
		    String email
	){}
	
	public record ChangePasswordRequest(
			@NotBlank
		    String currentPassword,

		    @NotBlank
		    @Size(min = 8, max = 64)
		    String newPassword,

		    @NotBlank
		    String confirmPassword
	) {}

	public record UserInfoResponse(
			UUID uuid,
			String name,
			String email,
			Set<String> roles
	) {}
}

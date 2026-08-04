package com.ticket_booking.user.commands;

public class UserCommands {
	
	public record UpdateUserCommand(
		Long id,
		String name,
		String email
	) {}
	
	public record ChangePasswordCommand(
		Long userId,
		String currentPassword,
		String newPassword,
		String confirmPassword
	) {}
}

package com.ticket_booking.auth.commands;

public final class AuthenticationCommands {
	
	private AuthenticationCommands() {}

	public record AuthenticateUserCommand(
			String email, 
			String password
	) {};
	
	public record RegisterUserCommand(
			String email, 
			String password, 
			String name
	) {};
}

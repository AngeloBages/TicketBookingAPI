package com.ticket_booking.common.security.services.commands;

public class AuthenticationCommands {

	public record AuthenticateUserCommand(String email, String password) {};
	
	public record RegisterUserCommand(String email, String password, String name) {};
}

package com.ticket_booking.common.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.common.security.dtos.AuthenticationDtos.AuthResponse;
import com.ticket_booking.common.security.dtos.AuthenticationDtos.AuthenticateUserRequest;
import com.ticket_booking.common.security.dtos.AuthenticationDtos.RefreshTokenRequest;
import com.ticket_booking.common.security.dtos.AuthenticationDtos.RegisterUserRequest;
import com.ticket_booking.common.security.services.AuthService;
import com.ticket_booking.common.security.services.commands.AuthenticationCommands.AuthenticateUserCommand;
import com.ticket_booking.common.security.services.commands.AuthenticationCommands.RegisterUserCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/public/auth")
@Tag(name = "Public Authentication", description = "Public authentication endpoints")
public class PublicAuthController {

	private final AuthService authService;
	
	public PublicAuthController(
			AuthService authService) {
		
		this.authService = authService;
	}
	
	@PostMapping("register")
	@Operation(
			summary = "Register a new user",
			description = "Creates a new account and returns JWT and Refresh Token.")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		
		return ResponseEntity.ok(
				authService.register(
					new RegisterUserCommand(
						request.email(), 
						request.password(), 
						request.name()
					)
				)
			);
	}
	
	@PostMapping("login")
	@Operation(
			summary = "Authenticate user",
			description = "Authenticates a user using email/password.")
	public ResponseEntity<AuthResponse> logIn(@Valid @RequestBody AuthenticateUserRequest request){
		
		return ResponseEntity.ok(
				authService.login(
					new AuthenticateUserCommand(
						request.email(),
						request.password()
					)
				)
			);
	}
	
	@PostMapping("refresh")
	@Operation(
			summary = "Refresh JWT",
			description = "Uses a valid Refresh Token to obtain a new Access Token.")
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
	
		return ResponseEntity.ok(
				authService.refresh(
						request.refreshToken()
				)
			);
	}
}

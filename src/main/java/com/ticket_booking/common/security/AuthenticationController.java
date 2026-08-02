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
import com.ticket_booking.common.security.services.AuthenticationService;
import com.ticket_booking.common.security.services.JwtService;
import com.ticket_booking.common.security.services.RefreshTokenService;
import com.ticket_booking.common.security.services.commands.AuthenticationCommands.AuthenticateUserCommand;
import com.ticket_booking.common.security.services.commands.AuthenticationCommands.RegisterUserCommand;
import com.ticket_booking.domain.models.RefreshToken;
import com.ticket_booking.domain.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/public/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthenticationController {

	private final AuthenticationService authenticationService;
	private final RefreshTokenService refreshTokenService;
	private final JwtService jwtService;
	
	public AuthenticationController(
			AuthenticationService authenticationService,
			RefreshTokenService refreshTokenService,
			JwtService jwtService) {
		
		this.authenticationService = authenticationService;
		this.refreshTokenService = refreshTokenService;
		this.jwtService = jwtService;
	}
	
	@PostMapping("register")
	@Operation(
			summary = "Register a new user",
			description = "Creates a new account and returns JWT and Refresh Token.")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		
		User user = authenticationService.registerUser(
				new RegisterUserCommand(
						request.email(), 
						request.password(), 
						request.name()
		));
		
		String accessToken = jwtService.generateToken(new AppUser(user));
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
	}
	
	@PostMapping("login")
	@Operation(
			summary = "Authenticate user",
			description = "Authenticates a user using email/password.")
	public ResponseEntity<AuthResponse> logIn(@Valid @RequestBody AuthenticateUserRequest request){
		
		AppUser userDetails = authenticationService.authenticate(
				new AuthenticateUserCommand(
						request.email(),
						request.password()
				)
		);
		
		String accessToken = jwtService.generateToken(userDetails);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUser());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
	}
	
	@PostMapping("refresh")
	@Operation(
			summary = "Refresh JWT",
			description = "Uses a valid Refresh Token to obtain a new Access Token.")
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
	
		RefreshToken newRefreshToken = refreshTokenService.rotateToken(request.refreshToken());
		
		User user = newRefreshToken.getUser();
        AppUser userDetails = new AppUser(user);
                    
         String newAccessToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken.getToken()));
	}
}

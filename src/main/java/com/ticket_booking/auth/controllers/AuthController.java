package com.ticket_booking.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.auth.AuthService;
import com.ticket_booking.auth.requests.AuthenticationRequests.LogoutRequest;
import com.ticket_booking.common.security.AppUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth")
@Tag(name = "Authentication", description = "Private authentication endpoints")
public class AuthController {
	

	private final AuthService authService;
	
	public AuthController(AuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping("logout")
	@Operation(
			summary = "Logout user",
			description = "Revokes the authenticated user's valid Refresh Token")
	public ResponseEntity<Void> logout(
			@AuthenticationPrincipal AppUser user,
			@Valid @RequestBody LogoutRequest request) {
		
		authService.logout(
				user.getUser().getId(), 
				request.refreshToken()
		);
		
		return ResponseEntity.noContent().build();
	}
}

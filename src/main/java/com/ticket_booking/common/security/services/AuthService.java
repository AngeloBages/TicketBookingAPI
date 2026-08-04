package com.ticket_booking.common.security.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.exceptions.EmailAlreadyInUseException;
import com.ticket_booking.common.security.AppUser;
import com.ticket_booking.common.security.dtos.AuthenticationDtos.AuthResponse;

import static com.ticket_booking.common.security.services.commands.AuthenticationCommands.*;

import java.util.HashSet;

import com.ticket_booking.domain.models.RefreshToken;
import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.repositories.IUserRepository;


@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final IUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public AuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			IUserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public AuthResponse login(AuthenticateUserCommand command) {
		
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						command.email(), 
						command.password())
				);
		
		AppUser userDetails = (AppUser) authentication.getPrincipal();
		
		return issueTokens(userDetails.getUser());
	}
	
	@Transactional
    public AuthResponse register(RegisterUserCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyInUseException(command.email());
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        User newUser = User.create(
        		command.name(), 
        		command.email(), 
        		hashedPassword, 
        		new HashSet<>());
        
        newUser = userRepository.save(newUser);

		return issueTokens(newUser);
	}
	
	@Transactional
	public AuthResponse refresh(String refreshToken) {

		RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken);

		User user = newRefreshToken.getUser();
		String newAccessToken = jwtService.generateToken(new AppUser(user));
		
		return new AuthResponse(newAccessToken, newRefreshToken.getToken());
	}
	
	@Transactional
	public void logout(Long userId, String refreshToken) {
		refreshTokenService.revokeTokenFromUser(userId, refreshToken);
	}
	
	private AuthResponse issueTokens(User user) {

	    AppUser principal = new AppUser(user);

	    String accessToken = jwtService.generateToken(principal);
	    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

	    return new AuthResponse(
	            accessToken,
	            refreshToken.getToken());
	}
}

package com.ticket_booking.common.security.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.exceptions.EmailAlreadyInUseException;
import com.ticket_booking.common.security.AppUser;
import static com.ticket_booking.common.security.services.commands.AuthenticationCommands.*;

import java.util.HashSet;

import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.repositories.IUserRepository;


@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final IUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public AuthService(
			AuthenticationManager authenticationManager,
			IUserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
    public User registerUser(RegisterUserCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyInUseException(command.email());
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        User newUser = User.create(
        		command.name(), 
        		command.email(), 
        		hashedPassword, 
        		new HashSet<>());

        return userRepository.save(newUser);
	}
	
	public AppUser authenticate(AuthenticateUserCommand command) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(command.email(), command.password())
				);
		
		return (AppUser) authentication.getPrincipal();
	}
}

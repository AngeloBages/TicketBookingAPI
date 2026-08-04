package com.ticket_booking.user.services;

import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.exceptions.EmailAlreadyInUseException;
import com.ticket_booking.common.exceptions.InvalidCurrentPasswordException;
import com.ticket_booking.common.exceptions.PasswordConfirmationException;
import com.ticket_booking.common.exceptions.SamePasswordException;
import com.ticket_booking.common.exceptions.UserNotFoundException;
import com.ticket_booking.common.security.services.RefreshTokenService;
import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.commands.UserCommands.ChangePasswordCommand;
import com.ticket_booking.user.commands.UserCommands.UpdateUserCommand;
import com.ticket_booking.user.dtos.UserDtos.UserInfoResponse;
import com.ticket_booking.user.repositories.IUserRepository;


@Service
public class UserService {

	private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UserService(
            IUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }
    
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(Long id) {

        User user = findUser(id);

        return new UserInfoResponse(
        		user.getUuid(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );
    }
	
	@Transactional
	public void updateUserInfo(UpdateUserCommand command) {
		
		User user = findUser(command.id());
		
		if (!user.getEmail().equalsIgnoreCase(command.email())
	            && userRepository.existsByEmail(command.email())) {

	        throw new EmailAlreadyInUseException(command.email());
	    }

		user.changeName(command.name());
		user.changeEmail(command.email());
	}
	
	@Transactional
	public void changePassword(ChangePasswordCommand command) {
		
		User user = findUser(command.userId());
		
		if (!passwordEncoder.matches(
	            command.currentPassword(),
	            user.getPassword())) {

	        throw new InvalidCurrentPasswordException();
	    }

	    if (!command.newPassword().equals(command.confirmPassword())) {
	        throw new PasswordConfirmationException();
	    }

	    if (passwordEncoder.matches(
	            command.newPassword(),
	            user.getPassword())) {

	        throw new SamePasswordException();
	    }

	    String encodedPassword =
	            passwordEncoder.encode(command.newPassword());

	    user.changePassword(encodedPassword);

	    refreshTokenService.revokeAllUserTokens(user.getId());
	}
	
	@Transactional
	public void deleteUser(Long userId) {

	    User user = findUser(userId);

	    refreshTokenService.revokeAllUserTokens(user.getId());

	    userRepository.delete(user);
	}
	
	private User findUser(Long id) {

	    return userRepository.findById(id)
	            .orElseThrow(() -> new UserNotFoundException());
	}
}

package com.ticket_booking.admin;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.admin.exceptions.LastAdminUserException;
import com.ticket_booking.admin.responses.AdminResponses.UserInfoResponse;
import com.ticket_booking.common.AppRole;
import com.ticket_booking.domain.models.Role;
import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.exceptions.RoleNotFoundException;
import com.ticket_booking.user.exceptions.UserNotFoundException;
import com.ticket_booking.user.repositories.IRoleRepository;
import com.ticket_booking.user.repositories.IUserRepository;

@Service
public class UserAdministrationService {

	private final IUserRepository userRepository;
	private final IRoleRepository roleRepository;
	
	public UserAdministrationService(
			IUserRepository userRepository,
			IRoleRepository roleRepository) {
		
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}
	
	@Transactional(readOnly = true)
	public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
		
		return userRepository.findAll(pageable)
				.map(user -> toResponse(user));
	}

	@Transactional(readOnly = true)
	public UserInfoResponse getUserInfo(UUID userId) {
		
		User user = getUser(userId);
		return toResponse(user);
	}
	
	@Transactional
	public void assignRoleToUser(UUID userId, AppRole roleName) {
		User user = getUser(userId);
		Role role = getRole(roleName);
		
		user.assignRole(role);
	}
	
	@Transactional
	public void revokeRoleFromUser(UUID userId, AppRole roleName) {
		User user = getUser(userId);
		Role role = getRole(roleName);
		
		if (roleName == AppRole.ROLE_ADMIN &&
			user.hasRole(role)) {
			
			long numberOfAdmins = userRepository.countByRolesName(roleName.name());

			if(numberOfAdmins == 1)
				throw new LastAdminUserException();
		}
		
		user.revokeRole(role);
	}
	
	private UserInfoResponse toResponse(User user) {
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
	
	private User getUser(UUID uuid) {
	    return userRepository.findByUuid(uuid)
	        .orElseThrow(UserNotFoundException::new);
	}
	
	private Role getRole(AppRole role) {
	    return roleRepository.findByName(role.name())
	            .orElseThrow(() ->
	                new RoleNotFoundException(role.name()));
	}
}

package com.ticket_booking.common.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.repositories.IUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private final IUserRepository userRepository;
	
	public AppUserDetailsService(IUserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
		
		return new AppUser(user);
	}

}

package com.ticket_booking.user.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket_booking.domain.models.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	public Optional<User> findByEmail(String email);
	
	public Optional<User> findByUuid(UUID uuid);
	
	public long countByRolesName(String roleName);
	
	public boolean existsByEmail(String email);
}

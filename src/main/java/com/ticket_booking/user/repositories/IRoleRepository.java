package com.ticket_booking.user.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket_booking.domain.models.Role;

public interface IRoleRepository extends JpaRepository<Role, Long> {
	
	public Optional<Role> findByName(String name);
}

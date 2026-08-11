package com.ticket_booking.user.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket_booking.domain.models.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	@EntityGraph(attributePaths = {"roles"})
	public Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"roles"})
	public Optional<User> findByUuid(UUID uuid);
	
	@Override
    @EntityGraph(attributePaths = {"roles"})
    public Page<User> findAll(Pageable pageable);
	
	public long countByRolesName(String roleName);
	
	public boolean existsByEmail(String email);
}

package com.ticket_booking.user.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ticket_booking.domain.models.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	
	@Query("SELECT u FROM User u WHERE u.email = :email")
	public Optional<User> findByEmail(@Param("email") String email);
	
	@Query("SELECT u FROM User u WHERE u.uuid = :uuid")
	public Optional<User> findByUuid(@Param("uuid") UUID userId);
	
	@Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
	public long countUsersByRoleName(@Param("roleName") String roleName);
	
	boolean existsByEmail(String email);
}

package com.ticket_booking.user.repositories;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ticket_booking.domain.models.User;

public interface IUserRepository extends JpaRepository<User, Long> {

	
	@Query("SELECT u FROM User u WHERE u.email = :email")
	public Optional<User> findByEmail(@Param("email") String email);
	
	boolean existsByEmail(String email);
}

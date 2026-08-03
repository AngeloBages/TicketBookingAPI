package com.ticket_booking.common.security.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.RefreshToken;


public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	@Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);
	
	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.revoked = true " +
		   "WHERE rt.token = :token AND rt.user.id = :userId AND rt.revoked = false")
	int revokeTokenFromUser(@Param("userId") Long userId, @Param("token") String token);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true " +
           "WHERE rt.user.id = :userId AND rt.revoked = false")
    void revokeAllUserTokens(@Param("userId") Long userId);
}

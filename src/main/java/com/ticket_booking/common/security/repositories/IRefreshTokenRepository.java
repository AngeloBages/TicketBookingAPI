package com.ticket_booking.common.security.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.RefreshToken;

import jakarta.persistence.LockModeType;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true " +
           "WHERE rt.user.uuid = :userId AND rt.revoked = false")
    void revokeAllUserTokens(@Param("userId")UUID userId);
}

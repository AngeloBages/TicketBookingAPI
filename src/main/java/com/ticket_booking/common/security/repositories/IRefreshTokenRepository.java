package com.ticket_booking.common.security.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.RefreshToken;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true " +
           "WHERE rt.user.uuid = :userId AND rt.revoked = false")
    void revokeAllUserTokens(@Param("userId")UUID userId);
}

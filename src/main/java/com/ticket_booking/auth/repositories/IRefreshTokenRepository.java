package com.ticket_booking.auth.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.RefreshToken;


public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	@Query("""
		SELECT rt 
		FROM RefreshToken rt 
		JOIN FETCH rt.user 
		WHERE rt.token = :token
		""")
    Optional<RefreshToken> findByToken(@Param("token") String token);
	
	@Modifying
	@Query("""
		UPDATE RefreshToken rt 
		SET rt.revoked = true,
			rt.replacedByTokenId = :replacedByTokenId,
			rt.revokedAt = :now
		WHERE rt.id = :id
			AND rt.revoked = false
		""")
	int revokeToken(
			@Param("id") Long id, 
			@Param("replacedByTokenId") Long replacedByTokenId,
			@Param("now") Instant now);
	
	@Modifying
	@Query("""
		UPDATE RefreshToken rt 
		SET rt.revoked = true,
			rt.revokedAt = :now
		WHERE rt.token = :token 
			AND rt.user.id = :userId 
			AND rt.revoked = false
		""")
	int revokeTokenFromUser(
			@Param("userId") Long userId, 
			@Param("token") String token,
			@Param("now") Instant now);
    
    @Modifying
    @Query("""
    	UPDATE RefreshToken rt 
    	SET rt.revoked = true,
    		rt.revokedAt = :now
        WHERE rt.user.id = :userId 
    		   AND rt.revoked = false
    	""")
    int revokeAllUserTokens(@Param("userId") Long userId, @Param("now") Instant now);
}

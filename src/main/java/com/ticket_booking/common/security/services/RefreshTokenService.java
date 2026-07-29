package com.ticket_booking.common.security.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.security.properties.SecurityProperties;
import com.ticket_booking.common.security.repositories.IRefreshTokenRepository;
import com.ticket_booking.domain.models.RefreshToken;
import com.ticket_booking.domain.models.User;

@Service
public class RefreshTokenService {

	private final IRefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;

    public RefreshTokenService(
    		IRefreshTokenRepository refreshTokenRepository, 
    		SecurityProperties securityProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
    	// revokeAllUserTokens() method could be called here, but that would disable the refresh token for multiple devices
    	
        String tokenString = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(securityProperties.refreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken(tokenString, user, expiry);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
    	if (token.isRevoked()) {
            throw new RuntimeException("Security Alert: Replay attack detected! Token was already used.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token was expired.");
        }
        
        return token;
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }	
}

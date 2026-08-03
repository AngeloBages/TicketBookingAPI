package com.ticket_booking.common.security.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.exceptions.InvalidRefreshTokenException;
import com.ticket_booking.common.exceptions.RefreshTokenExpiredException;
import com.ticket_booking.common.exceptions.RefreshTokenNotFoundException;
import com.ticket_booking.common.exceptions.RefreshTokenReplayException;
import com.ticket_booking.common.security.properties.SecurityProperties;
import com.ticket_booking.common.security.repositories.IRefreshTokenRepository;
import com.ticket_booking.domain.models.RefreshToken;
import com.ticket_booking.domain.models.User;

@Service
public class RefreshTokenService {
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final IRefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;

    public RefreshTokenService(
    		IRefreshTokenRepository refreshTokenRepository, 
    		SecurityProperties securityProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
    }
    
    @Transactional
    public RefreshToken rotateToken(String token) {
    	RefreshToken refreshToken = findByToken(token)
				.orElseThrow(() -> new RefreshTokenNotFoundException(token));
				
        verifyExpiration(refreshToken);
        refreshToken.setRevoked(true);
        
        try {
            return createRefreshToken(refreshToken.getUser());
        }
        catch (ObjectOptimisticLockingFailureException ex) {
            throw new RefreshTokenReplayException(
                    "Refresh token was already used."
            );
        }
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
    	String token = generateRefreshToken();
    	
        Instant expiry = Instant.now().plusMillis(securityProperties.refreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken(token, user, expiry);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }	

    @Transactional
	public void revokeTokenFromUser(Long userId, String token) {
		int updates = refreshTokenRepository.revokeTokenFromUser(userId, token);
		
		if(updates == 0) {
			throw new InvalidRefreshTokenException();
		}
	}

    private void verifyExpiration(RefreshToken token) {
    	if (token.isRevoked()) {
    		refreshTokenRepository.revokeAllUserTokens(token.getUser().getId());
            throw new RefreshTokenReplayException("Replay attack detected for refresh token.");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
        	refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException();
        }
    }
    
    private String generateRefreshToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

}

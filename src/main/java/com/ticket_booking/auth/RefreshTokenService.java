package com.ticket_booking.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.auth.exceptions.RefreshTokenExpiredException;
import com.ticket_booking.auth.exceptions.RefreshTokenNotFoundException;
import com.ticket_booking.auth.exceptions.RefreshTokenReplayException;
import com.ticket_booking.auth.repositories.IRefreshTokenRepository;
import com.ticket_booking.auth.responses.AuthenticationResponses.RefreshTokenResult;
import com.ticket_booking.common.security.properties.SecurityProperties;
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
    public RefreshTokenResult rotateToken(String rawToken) {
    	String tokenHash = hashToken(rawToken);
    	RefreshToken refreshToken = findByToken(tokenHash);
    	
        Instant now = Instant.now();

    	if (refreshToken.isExpired(now)) {
        	refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException();
        }
    	
    	if (refreshToken.isRevoked()) {
    		revokeAllUserTokens(refreshToken.getUser().getId());
            throw new RefreshTokenReplayException();
        }
    	
    	RefreshTokenResult replacement = createRefreshToken(refreshToken.getUser());
    	
    	int updates = refreshTokenRepository.revokeToken(
    			refreshToken.getId(), 
    			replacement.token().getId(),
    			now
    	);
		
		if(updates == 0) {
			refreshTokenRepository.delete(replacement.token());
			throw new RefreshTokenNotFoundException();
		}
		
		return replacement;
    }

    @Transactional
    public RefreshTokenResult createRefreshToken(User user) {
    	String rawToken = generateRandomSecureString();
    	String tokenHash = hashToken(rawToken);
    	
        Instant expiry = Instant.now()
        		.plusMillis(securityProperties.refreshTokenExpiration());

        RefreshToken refreshToken = RefreshToken.create(
        		tokenHash, 
        		user, 
        		expiry
        );
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return new RefreshTokenResult(refreshToken, rawToken);
    }

    @Transactional
	public void revokeTokenFromUser(Long userId, String rawToken) {
    	String tokenHash = hashToken(rawToken);
    	
		int updates = refreshTokenRepository
				.revokeTokenFromUser(userId, tokenHash, Instant.now());
		
		if(updates == 0) {
			throw new RefreshTokenNotFoundException();
		}
	}

   public void revokeAllUserTokens(Long userId) {
	   refreshTokenRepository.revokeAllUserTokens(userId, Instant.now());
   }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
        		.orElseThrow(() -> new RefreshTokenNotFoundException(token));
    }	
    
    public RefreshToken findById(Long id) {
    	return refreshTokenRepository.findById(id)
    			.orElseThrow(() -> new RefreshTokenNotFoundException());
    }
    
    private String generateRandomSecureString() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
    
    private String hashToken(String rawToken) {
    	try {
    		MessageDigest digest = MessageDigest.getInstance("SHA-256");
    		byte[] encodedHash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
    		return Base64.getUrlEncoder()
    				.withoutPadding()
    				.encodeToString(encodedHash);
    		
    	} catch(NoSuchAlgorithmException ex) {
    		throw new RuntimeException("SHA-256 algorithm not available", ex);
    	}
    }
}

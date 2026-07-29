package com.ticket_booking.common.security.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ticket_booking.common.security.properties.SecurityProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	private final SecurityProperties securityProperties;
	
	public JwtService(SecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(securityProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
	
	public String extractEmail(String token) {
		return extractClaim(token, (claims) -> claims.get("email", String.class));
	}
	
	public boolean isTokenExpired(String token) {
		Date exp = extractClaim(token, (claims) -> claims.getExpiration());
		return exp.before(new Date());	
	}
	
	public boolean isTokenValid(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return !isTokenExpired(token);
		}catch(Exception ex) {
			return false;
		}
	}

	public String generateToken(UserDetails userDetails) {
		
		Map<String, Object> claims = new HashMap<>();
		
		claims.put("email", userDetails.getUsername());
		claims.put("roles", userDetails.getAuthorities().stream()
				.map(authority -> authority.getAuthority())
				.toList());
		
		long now = System.currentTimeMillis();
		
		return Jwts.builder()
				.claims(claims)
				.subject(userDetails.getUsername())
				.issuedAt(new Date(now))
				.expiration(new Date(now + securityProperties.accessTokenExpiration()))
				.signWith(getSigningKey())
				.compact();
	}
}

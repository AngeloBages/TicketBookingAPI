package com.ticket_booking.domain.models;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
    @SequenceGenerator(name = "refresh_token_seq", sequenceName = "refresh_token_seq_generator")
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Version
    @Column(nullable = false)
    private Long version;

    
    protected RefreshToken() {}
    
    private RefreshToken(
            String token,
            User user,
            Instant expiresAt) {
    	
        this.token = Objects.requireNonNull(token);
        this.user = Objects.requireNonNull(user);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.revoked = false;
    }

    public static RefreshToken create(
    		String token, 
    		User user, 
    		Instant expiresAt) {
    	
        return new RefreshToken(
        		token,
        		user,
        		expiresAt
        	);
    }
    
	public void revoke() {
		this.revoked = true;
	}
	
	public boolean isExpired(Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		return !this.expiresAt.isAfter(now);
	}
	
	public boolean isUsable(Instant now) {
		return !revoked && !isExpired(now);
	}

    public Long getId() { return this.id; }
    public String getToken() { return this.token; }
    public User getUser() { return this.user; }
    public Instant getExpiresAt() { return this.expiresAt; }
    public boolean isRevoked() { return revoked; }
    public Long getVersion() { return this.version; }
}

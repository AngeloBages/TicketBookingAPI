package com.ticket_booking.domain.models;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.ticket_booking.domain.models.valueobjects.EmailAddress;
import com.ticket_booking.domain.models.valueobjects.UserName;
import com.ticket_booking.user.exceptions.InvalidUserFieldException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "app_user_seq_generator", allocationSize = 50)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    
    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    
    protected User() { 
    	// JPA 
    } 
    
    private User(
    		UserName name, 
    		EmailAddress email, 
    		String encodedPassword, 
    		Set<Role> roles) {
    	
    	this.uuid = UUID.randomUUID(); 
    	
    	this.name = name.value(); 
    	this.email = email.value(); 
    	this.password = requireEncodedPassword(encodedPassword); 
    	
    	this.assignRoles(roles);
    } 
    
    public static User create(
    		String name, 
    		String email, 
    		String encodedPassword, 
    		Set<Role> roles ) { 
    	
    	return new User(
    			new UserName(name), 
    			new EmailAddress(email), 
    			encodedPassword, 
    			roles 
    		);
    } 
    
    public void changeName(String name) { 
    	this.name = new UserName(name).value(); 
    } 
    
    public void changeEmail(String email) { 
    	this.email = new EmailAddress(email).value(); 
    }
    
    public boolean hasEmail(String email) {

    	return this.email.equals(
    			new EmailAddress(email).value()
    	);
    }

    public void changePassword(String encodedPassword) {

        this.password = requireEncodedPassword(encodedPassword);
    }
    
    public void assignRole(Role role) {
    	this.roles.add(role);
    }
    
    public void assignRoles(Collection<Role> roles) {
        this.roles.addAll(roles);
    }

    public void revokeRole(Role role) {
    	if (role == null) { 
    		return; 
    	} 
    	
    	this.roles.remove(role);
    }
    
    public boolean hasRole(Role role) {
    	return role != null && this.roles.contains(role);
    }
    
	private static String requireEncodedPassword(String encodedPassword) {
		if (encodedPassword == null || encodedPassword.isBlank()) {
			throw new InvalidUserFieldException(
					"password", 
					"encoded password must not be blank"
				);
		}
		return encodedPassword;
	}

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
}

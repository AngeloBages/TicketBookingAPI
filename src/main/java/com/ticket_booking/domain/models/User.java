package com.ticket_booking.domain.models;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    
    @PrePersist
    public void onPrePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    
    protected User() {}
    
    public static User create(
            String name,
            String email,
            String encodedPassword,
            Set<Role> roles) {

        User user = new User();

        user.name = user.normalizeName(name);
        user.email = user.normalizeEmail(email);
        user.password = encodedPassword;
        user.roles.addAll(roles);

        return user;
    }
    
    public boolean hasEmail(String email) {

        return this.email.equalsIgnoreCase(email);
    }
    
    public void changeName(String name) {

        String normalized = normalizeName(name);

        if (normalized.equals(this.name)) {
            return;
        }

        this.name = normalized;
    }

    public void changeEmail(String email) {

        String normalized = normalizeEmail(email);

        if (normalized.equals(this.email)) {
            return;
        }

        this.email = normalized;
    }

    public void changePassword(String encodedPassword) {

        this.password = Objects.requireNonNull(encodedPassword);
    }
    
    public void assignRole(Role role) {
        roles.add(role);
    }

    public void revokeRole(Role role) {
        roles.remove(role);
    }
    
    public boolean hasRole(Role role) {
    	return this.roles.contains(role);
    }
    
    private String normalizeName(String name) {

        return name.trim()
                .replaceAll("\\s+", " ");
    }
    
    private String normalizeEmail(String email) {

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
}

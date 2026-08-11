package com.ticket_booking.domain.models;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_role")
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
	@SequenceGenerator(name = "role_seq", sequenceName = "role_seq_generator", allocationSize = 50)
	private Long id;
	
	@Column(nullable = false, unique = true)
	private String name;
	
	protected Role() {}
	
	public Role(String name) {
        this.name = name;
    }

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		return Objects.equals(name, other.name);
	}
	

	public Long getId() { return id; }
	public String getName() { return name; }
}

package com.ticket_booking.domain.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue")
public class Venue {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venue_seq")
	@SequenceGenerator(name = "venue_seq", sequenceName = "venue_seq_generator")
	private Long id;
	
	@Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String address;
	
	@PrePersist
    public void onPrePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
	}

	
	public UUID getUuid() {
    	return this.uuid;
    }
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}
}

package com.ticket_booking.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.ticket_booking.domain.models.valueobjects.VenueAddress;
import com.ticket_booking.domain.models.valueobjects.VenueName;
import com.ticket_booking.domain.models.valueobjects.VenueSeatRow;
import com.ticket_booking.venue.exceptions.DuplicateSeatException;
import com.ticket_booking.venue.exceptions.InvalidVenueStateException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
	
	@Column(nullable = false)
    private boolean active;
	
	@OneToMany(
    	    mappedBy = "venue",
    	    fetch = FetchType.LAZY,
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true
    	)
    private List<Seat> seats = new ArrayList<>();
	

	protected Venue() {
        // JPA
    }
	
    private Venue(
    		VenueName name, 
    		VenueAddress address) {
    	
    	this.uuid = UUID.randomUUID();
    	
        this.name = name.value();
        this.address = address.value();
        this.active = true;
    }
	
    public static Venue create(
            String name,
            String address
    ) {
        return new Venue(
                new VenueName(name),
                new VenueAddress(address)
        );
    }
	
    public void changeName(String name) {
        this.name = new VenueName(name).value();
    }

    public void changeAddress(String address) {
        this.address = new VenueAddress(address).value();
    }
    
	public void activate() {
		if(this.active) {
			throw new InvalidVenueStateException(
						"The venue is already active"
					);
		}
		this.active = true;
	}
	
	public void deactivate() {
		if(!this.active) {
			throw new InvalidVenueStateException(
						"The venue is already inactive"
					);
		}
		this.active = false;
	}
    
    public void addSeat(int number, String row) {
    	String normalizedRow = new VenueSeatRow(row).value();
    	
        boolean exists = seats.stream()
                .anyMatch(seat ->
                        seat.getNumber() == number &&
                        seat.getRow().equals(normalizedRow));

        if (exists) {
            throw new DuplicateSeatException(number, normalizedRow);
        }

        seats.add(Seat.create(
        		number, 
        		normalizedRow, 
        		this
        	)
        );
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
	
	public boolean isActive() {
		return this.active;
	}
	
	public List<Seat> getSeats() {
	    return Collections.unmodifiableList(seats);
	}
	
	public List<Seat> getActiveSeats() {
		return this.seats
				.stream()
				.filter(seat -> seat.isActive())
				.toList();
	}
}

package com.ticket_booking.domain.models;

import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.domain.models.valueobjects.VenueSeatRow;
import com.ticket_booking.venue.exceptions.InvalidSeatFieldException;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "seat",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_seat_position",
			columnNames = {"venue_id", "seat_row", "seat_number"}
		)
	}
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq")
	@SequenceGenerator(name = "seat_seq", sequenceName = "seat_seq_generator")
    private Long id;
    
    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "seat_number", nullable = false, updatable = false)
    private int number;

    @Column(name = "seat_row", nullable = false, length = 10, updatable = false)
    private String row;
    
    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false, updatable = false)
    private Venue venue;
    
    
    protected Seat() {}
    
    private Seat(
    		int number,
    		VenueSeatRow row,
    		Venue venue) {
    	
    	this.uuid = UUID.randomUUID();
    	
    	this.number = validateNumber(number);
    	this.row = row.value();
    	this.venue = Objects.requireNonNull(venue);
    	this.active = true;
    }
    
    public static Seat create(
    		int number,
    		String row,
    		Venue venue) {
    	
    	return new Seat(
    			number,
    			new VenueSeatRow(row),
    			venue
    		);
    }
    
    public void activate() {
        this.active = true;
    }
    
    public void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public int getNumber() { return number; }
    public String getRow() { return row; }
    public boolean isActive() { return active; }
    public Venue getVenue() { return venue; }
    
    public Long getVenueId() {
		return this.venue.getId();
	}
    
    private static int validateNumber(int number) {
        if (number < 1 || number > 5000) {
            throw new InvalidSeatFieldException(
                "number",
                "must be between 1 and 5000"
            );
        }

        return number;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Seat other)) {
            return false;
        }

        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}

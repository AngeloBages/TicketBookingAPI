package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.BookingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "booking")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
	@SequenceGenerator(name = "booking_seq", sequenceName = "booking_seq_generator")
	private Long id;
	
	@Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;
	
	@Column(nullable = false)
	private Instant bookedAt;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status;
	
	@Column(nullable = false)
	private BigDecimal totalPrice;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;
	
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "booking_seats", 
			joinColumns = @JoinColumn(name = "booking_id"),
			inverseJoinColumns = @JoinColumn(name = "seat_id")
	)
	private Set<Seat> seats = new HashSet<>();
	
	
	@PrePersist
    public void onPrePersist() {
		if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
		
        this.bookedAt = Instant.now();
    }
	

	public UUID getUuid() {
		return this.uuid;
	}
	
	public BookingStatus getStatus() {
		return this.status;
	}
	
	public Instant getBookedAt() {
		return bookedAt;
	}

	public void setBookedAt(Instant bookedAt) {
		this.bookedAt = bookedAt;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	public void setSeats(Set<Seat> seats) {
		this.seats = seats;
	}

	public Long getId() {
		return id;
	}
}

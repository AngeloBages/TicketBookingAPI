package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.booking.exceptions.DuplicateBookingSeatException;
import com.ticket_booking.booking.exceptions.InvalidBookingStateException;
import com.ticket_booking.domain.models.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
	
	@OneToMany(
		    mappedBy = "booking",
		    fetch = FetchType.LAZY,
		    cascade = CascadeType.ALL,
		    orphanRemoval = true
		)
	private List<BookingSeat> bookingSeats = new ArrayList<>();
	
	
	@PrePersist
    public void onPrePersist() {
        this.bookedAt = Instant.now();
    }
	
	
	protected Booking() {}
	
	private Booking(
			User user,
	        Event event) {
		
		this.uuid = UUID.randomUUID();
	    
		this.status = BookingStatus.PENDING;
	    this.totalPrice = BigDecimal.ZERO;
		this.user = Objects.requireNonNull(user);
		this.event = Objects.requireNonNull(event);
		
		event.ensureBookable();
	}
	
	public static Booking create(
	        User user,
	        Event event) {

	    return new Booking(
	    		user,
	    		event
	    );
	}
	
	public void addSeat(EventSeat eventSeat, BigDecimal price) {

	    Objects.requireNonNull(eventSeat);
	    Objects.requireNonNull(price);
	    
	    if (!isFromSameEvent(eventSeat)) {
	        throw new InvalidBookingStateException(
	                "The seat and booking must belong to the same event"
	        );
	    }
		
		boolean exists = bookingSeats.stream()
                .anyMatch(bookingSeat -> Objects.equals(
                		bookingSeat.getEventSeat(),
                		eventSeat
                	)
                );

        if (exists) {
            throw new DuplicateBookingSeatException();
        }

	    BookingSeat bookingSeat =
	            BookingSeat.create(
	            		this, 
	            		eventSeat
	            );

	    bookingSeats.add(bookingSeat);
	    
	    this.totalPrice = totalPrice.add(eventSeat.getPrice());
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

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public User getUser() {
		return user;
	}

	public Event getEvent() {
		return event;
	}
	
	public Long getEventId() {
		return this.event.getId();
	}

	public Long getId() {
		return id;
	}
	
	public List<BookingSeat> getBookingSeats() {
	    return Collections.unmodifiableList(bookingSeats);
	}
	
	private boolean isFromSameEvent(EventSeat eventSeat) {
	    return Objects.equals(
	            this.event.getId(),
	            eventSeat.getEventId()
	    );
	}
}

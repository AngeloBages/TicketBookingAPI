package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.booking.exceptions.DuplicateBookingSeatException;
import com.ticket_booking.booking.exceptions.EventSeatNotAvailableException;
import com.ticket_booking.booking.exceptions.InvalidBookingStateException;
import com.ticket_booking.domain.events.booking.BookingCanceledEvent;
import com.ticket_booking.domain.events.booking.BookingExpiredEvent;
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
public class Booking extends AggregateRoot{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
	@SequenceGenerator(name = "booking_seq", sequenceName = "booking_seq_generator")
	private Long id;
	
	@Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;
	
	@Column(nullable = false, updatable = false)
	private Instant bookedAt;
	
	@Column
	private Instant expiresAt;
	
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
	        Event event,
	        Instant expiresAt) {
		
		this.uuid = UUID.randomUUID();
	    
		this.status = BookingStatus.PENDING;
	    this.totalPrice = BigDecimal.ZERO;
	    this.expiresAt = Objects.requireNonNull(expiresAt);
		this.user = Objects.requireNonNull(user);
		this.event = Objects.requireNonNull(event);
		
		event.ensureBookable();
	}
	
	public static Booking create(
	        User user,
	        Event event,
	        Instant expiresAt,
	        Collection<EventSeat> seats) {

	    Booking booking =  new Booking(
	    		user,
	    		event,
	    		expiresAt
	    );
	    
	    for(EventSeat seat : seats) {
	    	booking.addSeat(seat);
	    }
	    
	    return booking;
	}
	
	public void addSeat(EventSeat eventSeat) {
	    Objects.requireNonNull(eventSeat);
	    Objects.requireNonNull(eventSeat.getPrice());
	    
	    if(!eventSeat.isAvailable()){
	    	throw new EventSeatNotAvailableException();
	    }
	    
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
	    
	    eventSeat.held();

	    BookingSeat bookingSeat =
	            BookingSeat.create(
	            		this, 
	            		eventSeat
	            );
	    
	    bookingSeats.add(bookingSeat);
	    this.totalPrice = totalPrice.add(eventSeat.getPrice());
	}
	
	public void cancel() {
		if (status != BookingStatus.PENDING) {
			throw new InvalidBookingStateException(
					"Booking cannot be canceled from status: " + status
			);
		}
		this.expiresAt = null;
	    status = BookingStatus.CANCELLED;
	    
	    registerEvent(
				new BookingCanceledEvent(
						id,
						uuid
				)
		);
	}
	
	public void expire() {
	    if (status != BookingStatus.PENDING) {
	    	throw new InvalidBookingStateException(
	    			"Booking cannot be expired from status: " + status
	    	);
	    }

	    this.expiresAt = null;
	    status = BookingStatus.EXPIRED;
	    
	    registerEvent(
				new BookingExpiredEvent(
						id,
						uuid
				)
		);
	}
	
	public void requestRefund() {

	    if (status != BookingStatus.CONFIRMED) {
	        throw new InvalidBookingStateException(
	                "Only a confirmed booking can be refunded"
	        );
	    }

	    this.expiresAt = null;
	    status = BookingStatus.REFUND_PENDING;
	}
	
	public void refund() {

	    if (status != BookingStatus.REFUND_PENDING) {
	        throw new InvalidBookingStateException(
	                "The booking is not waiting for a refund"
	        );
	    }
	    status = BookingStatus.REFUNDED;
	    
	    //registerEvent(new BookingRefundedEvent(bookingSeats.stream().map(BookingSeat::getId).toList()));
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
	
	public Instant expiresAt() {
		return expiresAt;
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
	
	public boolean isPending() {
		return status == BookingStatus.PENDING;
	}
	
	public List<Long> getBookingSeatsIds(){
		return bookingSeats.stream()
				.map(BookingSeat::getId)
				.toList();
	}
	
	private boolean isFromSameEvent(EventSeat eventSeat) {
	    return Objects.equals(
	            this.event.getId(),
	            eventSeat.getEventId()
	    );
	}
}

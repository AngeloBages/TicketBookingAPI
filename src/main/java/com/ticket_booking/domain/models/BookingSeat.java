package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.util.Objects;

import com.ticket_booking.booking.exceptions.InvalidBookingSeatStateException;

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
    name = "booking_seat",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_booking_event_seat",
            columnNames = {"booking_id", "event_seat_id"}
        )
    }
)
public class BookingSeat {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "booking_seat_seq"
    )
    @SequenceGenerator(
        name = "booking_seat_seq",
        sequenceName = "booking_seat_seq_generator"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_seat_id", nullable = false)
    private EventSeat eventSeat;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    
    protected BookingSeat() {}
    
    private BookingSeat(
            Booking booking,
            EventSeat eventSeat) {
    
    	this.booking = Objects.requireNonNull(booking);
        this.eventSeat = Objects.requireNonNull(eventSeat);

        if (!belongsToSameEvent()) {
            throw new InvalidBookingSeatStateException(
                    "The seat and booking must belong to the same event"
            );
        }

        this.price = eventSeat.getPrice();
    }
    
    public static BookingSeat create(
            Booking booking,
            EventSeat eventSeat) {
        
        return new BookingSeat(
        		booking, 
        		eventSeat
        	);
    }
    
	public Long getId() {
		return id;
	}

	public Booking getBooking() {
		return booking;
	}

	public Long getBookingId() {
		return this.booking.getId();
	}
	
	public EventSeat getEventSeat() {
		return eventSeat;
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	private boolean belongsToSameEvent() {
        return Objects.equals(
                booking.getEventId(),
                eventSeat.getEventId()
        );
    }
}

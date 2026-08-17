package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.EventSeatStatus;
import com.ticket_booking.domain.models.valueobjects.EventPrice;
import com.ticket_booking.event.exceptions.InvalidEventSeatStateException;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(
    name = "event_seat",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_event_seat",
            columnNames = {"event_id", "seat_id"}
        )
    }
)
public class EventSeat {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "event_seat_seq"
    )
    @SequenceGenerator(
        name = "event_seat_seq",
        sequenceName = "event_seat_seq_generator"
    )
    private Long id;
    
    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSeatStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false, updatable = false)
    private Seat seat;
    
    @Version
    private long version;
    
    
    protected EventSeat () {
    	// JPA only
    }
    
    private EventSeat(
    		Event event,
    		Seat seat,
    		EventPrice eventPrice) {
    	
    	if (!seat.isActive()) {
            throw new InvalidEventSeatStateException(
                    "An inactive seat cannot be associated with an event"
            );
        }

        if (!belongsToSameVenue(event, seat)) {
            throw new InvalidEventSeatStateException(
                    "The seat and event must belong to the same venue"
            );
        }
    	
    	this.uuid = UUID.randomUUID();
    	this.event = Objects.requireNonNull(event);
    	this.seat = Objects.requireNonNull(seat);
    	this.price = eventPrice.value();
    	this.status = EventSeatStatus.AVAILABLE;
    }
    
    public static EventSeat create(
    		Event event,
    		Seat seat,
    		BigDecimal price) {
    	
    	return new EventSeat(
    			event,
    			seat,
    			new EventPrice(price)
    		);
    }
    
    public void reserve() {
        if (status != EventSeatStatus.AVAILABLE) {
            throw new InvalidEventSeatStateException(
                    "The seat is not available"
            );
        }

        status = EventSeatStatus.RESERVED;
    }
    
    public void book() {
    	if (status != EventSeatStatus.RESERVED) {
    		throw new InvalidEventSeatStateException(
    				"Only a reserved seat can be booked");
    	}

    	status = EventSeatStatus.BOOKED;
    }
    
    public void release() {
    	if (status != EventSeatStatus.RESERVED
    			&& status != EventSeatStatus.BOOKED) {

    		throw new InvalidEventSeatStateException(
    				"The seat cannot be released from its current state");
    	}

    	status = EventSeatStatus.AVAILABLE;
    }
    
    public void changePrice(BigDecimal price) {
    	this.price = new EventPrice(price).value();
    }
    
    public boolean isAvailable() {
    	return this.status == EventSeatStatus.AVAILABLE;
    }
    
	public Long getId() {
		return id;
	}
	
	public UUID getUuid() {
		return this.uuid;
	}

	public Event getEvent() {
		return event;
	}
	
	public Long getEventId() {
		return this.event.getId();
	}

	public Seat getSeat() {
		return seat;
	}
	
	public Long getSeatId() {
		return this.seat.getId();
	}

	public EventSeatStatus getStatus() {
		return status;
	}
	
	public BigDecimal getPrice() {
		return this.price;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof EventSeat es))
			return false;
		return Objects.equals(uuid, es.uuid);
	}
	
	private boolean belongsToSameVenue(Event event, Seat seat) {
		return Objects.equals(
				event.getVenueId(),
				seat.getVenueId()
				);
	}
}

package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.domain.events.event.EventCanceledEvent;
import com.ticket_booking.domain.models.enums.EventStatus;
import com.ticket_booking.domain.models.valueobjects.EventDescription;
import com.ticket_booking.domain.models.valueobjects.EventPrice;
import com.ticket_booking.domain.models.valueobjects.EventTitle;
import com.ticket_booking.event.exceptions.DuplicateEventSeatException;
import com.ticket_booking.event.exceptions.InvalidEventFieldException;
import com.ticket_booking.event.exceptions.InvalidEventSeatStateException;
import com.ticket_booking.event.exceptions.InvalidEventStateException;
import com.ticket_booking.venue.exceptions.InvalidSeatStateException;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "event")
public class Event extends AggregateRoot {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
	@SequenceGenerator(name = "event_seq", sequenceName = "event_seq_generator")
    private Long id;
	
	@Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String title;
    
    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime startsAt;
    
    @Column(nullable = false)
    private LocalDateTime endsAt;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false, updatable = false)
    private Venue venue;
    
    @OneToMany(
    	    mappedBy = "event",
    	    fetch = FetchType.LAZY,
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true
    	)
    private List<EventSeat> eventSeats = new ArrayList<>();
    
    
    protected Event() {
    	// JPA only
    }
    
    private Event(
            EventTitle title,
            EventDescription description,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            EventPrice price,
            Venue venue) {

        this.uuid = UUID.randomUUID();

        this.title = title.value();
        this.description = description.value();
        this.price = price.value();
        this.status = EventStatus.DRAFT;
        this.venue = Objects.requireNonNull(venue);
        
        validateEventTimes(
        		startsAt, 
        		endsAt, 
        		venue.getTimeZone());
        
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }
    
    public static Event create(
            String title,
            String description,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            BigDecimal price,
            Venue venue,
            Collection<Seat> seats) {
    	
    	Event event = new Event(
                new EventTitle(title),
                new EventDescription(description),
                startsAt,
                endsAt,
                new EventPrice(price),
                venue
    	);
    	
    	for (Seat seat : seats) {
			event.addSeat(seat);
		}

        return event;
    }
    
    public void changeTitle(String title) {
    	ensureDraft();
        this.title = new EventTitle(title).value();
    }

    public void changeDescription(String description) {
    	ensureDraft();
        this.description = new EventDescription(description).value();
    }

    public void changeStartsAt(LocalDateTime startsAt) {
    	ensureDraft();
    	
    	validateEventTimes(
        		startsAt, 
        		this.endsAt, 
        		this.venue.getTimeZone());

        this.startsAt = startsAt;
    }
    
    public void changeEndsAt(LocalDateTime endsAt) {
    	ensureDraft();

    	validateEventTimes(
    			endsAt, 
        		this.startsAt, 
        		this.venue.getTimeZone());

        this.endsAt = endsAt;
    }

    public void changePrice(BigDecimal price) {
    	ensureDraft();
    	
    	BigDecimal newPrice = new EventPrice(price).value();

        eventSeats.forEach(eventSeat ->
            eventSeat.changePrice(newPrice)
        );

        this.price = newPrice;
    }
    
	public void addSeat(Seat seat) {
		Objects.requireNonNull(seat);
		
		if (!seat.isActive()) {
	        throw new InvalidSeatStateException(
	                "An inactive seat cannot be added to an event"
	        );
	    }
		
		if (!isFromSameVenue(seat)) {
	        throw new InvalidEventSeatStateException(
	                "The seat and event must belong to the same venue"
	        );
	    }
		
		boolean exists = eventSeats.stream()
                .anyMatch(eventSeat -> Objects.equals(
                		eventSeat.getSeatId(), 
                		seat.getId()
                	)
                );

        if (exists) {
            throw new DuplicateEventSeatException(
            		seat.getNumber(), 
            		seat.getRow()
            );
        }

		EventSeat eventSeat = EventSeat.create(
				this, 
				seat, 
				this.price
			);
		
		this.eventSeats.add(eventSeat);
	}
	
	public void cancel() {

		switch (status) {
		
		    case DRAFT ->
			    throw new InvalidEventStateException(
		                "A draft event cannot be canceled."
	            );

	        case SCHEDULED -> 
	        	status = EventStatus.CANCELLED;
	
	        case CANCELLED ->
	            throw new InvalidEventStateException(
	                "The event is already cancelled"
	            );
	
	        case COMPLETED ->
	            throw new InvalidEventStateException(
	                "A completed event cannot be canceled"
	            );
	    }
	    
	    registerEvent(new EventCanceledEvent(this.id));
	}
	
	public void schedule() {

	    if (status != EventStatus.DRAFT) {
	        throw new InvalidEventStateException(
	                "Only draft events can be scheduled"
	        );
	    }

	    status = EventStatus.SCHEDULED;
	}
	
	public void complete() {

	    if (status != EventStatus.SCHEDULED) {
	        throw new InvalidEventStateException(
	                "Only a scheduled event can be completed"
	        );
	    }

	    status = EventStatus.COMPLETED;
	}
	
	public void ensureBookable() {
	    if (status != EventStatus.SCHEDULED) {
	        throw new InvalidEventStateException(
	            "The event is not available for booking"
	        );
	    }
	}
    
    public UUID getUuid() {
    	return this.uuid;
    }

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getStartsAt() {
		return startsAt;
	}
	
	public LocalDateTime getEndsAt() {
		return endsAt;
	}
	
	public ZonedDateTime getStartsAtAtVenue() {
	    return startsAt.atZone(venue.getTimeZone());
	}

	public ZonedDateTime getEndsAtAtVenue() {
	    return endsAt.atZone(venue.getTimeZone());
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public EventStatus getStatus() {
		return this.status;
	}

	public Venue getVenue() {
		return venue;
	}
	
	public Long getVenueId() {
		return this.venue.getId();
	}

	public Long getId() {
		return id;
	}
	
	public List<EventSeat> getEventSeats() {
	    return Collections.unmodifiableList(eventSeats);
	}
	
	
	private void ensureDraft() {
	    if (status != EventStatus.DRAFT) {
	        throw new InvalidEventStateException(
	                "Only draft events can be modified"
	        );
	    }
	}
	
	private boolean isFromSameVenue(Seat seat) {
	    return Objects.equals(
	            this.venue.getId(),
	            seat.getVenueId()
	    );
	}
	
	private static void validateEventTimes(
	        LocalDateTime startsAt,
	        LocalDateTime endsAt,
	        ZoneId venueTimeZone) {

	    if (startsAt == null) {
	        throw new InvalidEventFieldException(
	                "startsAt",
	                "is required"
	        );
	    }

	    if (endsAt == null) {
	        throw new InvalidEventFieldException(
	                "endsAt",
	                "is required"
	        );
	    }

	    ZonedDateTime zonedStartsAt = startsAt.atZone(venueTimeZone);
	    ZonedDateTime zonedEndsAt = endsAt.atZone(venueTimeZone);
	    ZonedDateTime now = ZonedDateTime.now(venueTimeZone);

	    if (!zonedStartsAt.isAfter(now)) {
	        throw new InvalidEventFieldException(
	                "startsAt",
	                "must be in the future"
	        );
	    }

	    if (!zonedEndsAt.isAfter(zonedStartsAt)) {
	        throw new InvalidEventFieldException(
	                "endsAt",
	                "must be after startsAt"
	        );
	    }
	}
}

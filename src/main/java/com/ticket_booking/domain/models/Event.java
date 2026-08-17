package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
public class Event {

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
    private LocalDate date;
    
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
            LocalDate date,
            EventPrice price,
            Venue venue) {

        this.uuid = UUID.randomUUID();

        this.title = title.value();
        this.description = description.value();
        this.date = validateEventDate(date);
        this.price = price.value();
        this.status = EventStatus.DRAFT;
        this.venue = Objects.requireNonNull(venue);
    }
    
    public static Event create(
            String title,
            String description,
            LocalDate date,
            BigDecimal price,
            Venue venue,
            Collection<Seat> seats) {
    	
    	Event event = new Event(
                new EventTitle(title),
                new EventDescription(description),
                date,
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

    public void changeDate(LocalDate date) {
    	ensureDraft();
        this.date = validateEventDate(date);
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

	        case SCHEDULED -> status = EventStatus.CANCELLED;
	
	        case CANCELLED ->
	            throw new InvalidEventStateException(
	                "The event is already cancelled"
	            );
	
	        case COMPLETED ->
	            throw new InvalidEventStateException(
	                "A completed event cannot be canceled"
	            );
	    }
	    
	    //Propagete domain event?
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

	public LocalDate getDate() {
		return date;
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
	
	private static LocalDate validateEventDate(LocalDate date) {
		if (date == null) {
            throw new InvalidEventFieldException(
            		"date", "is required");
        }

        if (!date.isAfter(LocalDate.now())) {
            throw new InvalidEventFieldException(
                    "date", "must be in the future"
            );
        }
        return date;
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
}

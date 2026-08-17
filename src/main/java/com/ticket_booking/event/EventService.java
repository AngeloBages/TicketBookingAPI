package com.ticket_booking.event;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.CursorPage;
import com.ticket_booking.domain.models.Event;
import com.ticket_booking.domain.models.Venue;
import com.ticket_booking.event.commands.EventCommands.CreateEventCommand;
import com.ticket_booking.event.commands.EventCommands.UpdateEventCommand;
import com.ticket_booking.event.exceptions.EventNotFoundException;
import com.ticket_booking.event.repositories.IEventRepository;
import com.ticket_booking.event.responses.EventResponses.EventResponse;
import com.ticket_booking.event.responses.EventResponses.EventSeatSummaryResponse;
import com.ticket_booking.event.responses.EventResponses.EventSummaryResponse;
import com.ticket_booking.event.utils.EventCursor;
import com.ticket_booking.event.utils.EventCursorParser;
import com.ticket_booking.event.utils.EventMapper;
import com.ticket_booking.venue.exceptions.InvalidVenueStateException;
import com.ticket_booking.venue.exceptions.VenueNotFoundException;
import com.ticket_booking.venue.repositories.IVenueRepository;

@Service
public class EventService {

	private final IEventRepository eventRepository;
	private final IVenueRepository venueRepository;
	
	public EventService(
			IEventRepository eventRepository,
			IVenueRepository venueRepository) {
		
		this.eventRepository = eventRepository;
		this.venueRepository = venueRepository;
	}
	
	@Transactional(readOnly = true)
	public CursorPage<EventSummaryResponse> getEvents(String cursor, int limit) {
		Pageable pageable = PageRequest.of(0, limit + 1);

        List<Event> events = Strings.isBlank(cursor)
        		? eventRepository.findFirstPage(pageable)
        		: findNextPage(cursor, pageable);
        
        if(events.isEmpty()) {
        	return new CursorPage<>(
        			List.of(),
        			null,
        			false
        	);
        }

        boolean hasNext = events.size() > limit;

        List<Event> page = hasNext
                ? events.subList(0, limit)
                : events;

        String nextCursor = hasNext
                ? createCursor(page.getLast())
                : null;

        List<EventSummaryResponse> responses = page.stream()
                .map(event -> EventMapper.toSummaryResponse(event))
                .toList();

        return new CursorPage<>(
                responses,
                nextCursor,
                hasNext
        );
    }
	
	@Transactional(readOnly = true)
	public EventResponse getEvent(UUID eventId) {
		Event event = eventRepository.findByUuidFetchVenueAndSeats(eventId)
				.orElseThrow(() -> new EventNotFoundException());
		
		return EventMapper.toResponse(event);
		
	}
	
	@Transactional(readOnly = true)
	public List<EventSeatSummaryResponse> getEventSeats(UUID eventId) {
		
		if (!eventRepository.existsByUuid(eventId)) {
	        throw new EventNotFoundException();
	    }
		
		return eventRepository.findSeatsByEventId(eventId)
					.stream()
					.map(seat -> new EventSeatSummaryResponse(
							seat.seatId(),
							seat.number(),
							seat.row(),
							seat.status(),
							seat.price()
					))
					.toList();
	}
	
	@Transactional
	public UUID createEvent(CreateEventCommand command) {
		Venue venue = findActiveVenueFetchSeats(command.venueId());
		
		Event event = Event.create(
				command.title(), 
				command.description(),
				command.date(),
				command.price(), 
				venue,
				venue.getActiveSeats()
		);
		
		eventRepository.save(event);
		
		return event.getUuid();
	}
	
	@Transactional
	public void updateEvent(UpdateEventCommand command) {
		Event event = findEvent(command.eventId());
		
		event.changeTitle(command.title());
		event.changeDescription(command.description());
		event.changeDate(command.date());
		event.changePrice(command.price());
	}
	
	@Transactional
	public void cancelEvent(UUID eventId) {
		Event event = findEvent(eventId);
		
		event.cancel();
	}
	
	@Transactional
	public void scheduleEvent(UUID eventId) {

	    Event event = findEvent(eventId);

	    event.schedule();
	}

    private String createCursor(Event event) {

        return EventCursorParser.encode(
                new EventCursor(
                        event.getDate(),
                        event.getId()
                )
        );
    }
    
    private List<Event> findNextPage(
            String cursor,
            Pageable pageable) {

        EventCursor decoded = EventCursorParser.decode(cursor);

        return eventRepository.findNextPage(
                decoded.eventDate(),
                decoded.eventId(),
                pageable
        );
    }
	
	private Event findEvent(UUID eventId) {
		return eventRepository.findByUuid(eventId)
				.orElseThrow(() -> new EventNotFoundException());
	}
	
	private Venue findActiveVenueFetchSeats(UUID venueId) {
	    Venue venue = venueRepository.findByUuidFetchSeats(venueId)
				.orElseThrow(() -> new VenueNotFoundException());

	    if (!venue.isActive()) {
	        throw new InvalidVenueStateException(
	                "An inactive venue cannot host an event"
	        );
	    }

	    return venue;
	}
}

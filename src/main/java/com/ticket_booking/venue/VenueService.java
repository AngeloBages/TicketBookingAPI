package com.ticket_booking.venue;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.CursorPage;
import com.ticket_booking.domain.models.Venue;
import static com.ticket_booking.venue.commands.VenueCommands.*;
import com.ticket_booking.venue.exceptions.VenueNotFoundException;
import com.ticket_booking.venue.repositories.IVenueRepository;
import com.ticket_booking.venue.responses.VenueResponses.VenueResponse;
import com.ticket_booking.venue.utils.VenueCursor;
import com.ticket_booking.venue.utils.VenueCursorParser;

@Service
public class VenueService {

	private final IVenueRepository venueRepository;
	
	public VenueService(IVenueRepository venueRepository) {
		this.venueRepository = venueRepository;
	}
	
	@Transactional(readOnly = true)
	public CursorPage<VenueResponse> getVenues(String cursor, int limit) {
		
		Pageable pageable = PageRequest.of(0, limit + 1);
		
		List<Venue> venues;
		
		if(Strings.isBlank(cursor)) {
			venues = venueRepository.findByOrderByIdAsc(pageable);
			
		} else {
			VenueCursor decoded = VenueCursorParser.decode(cursor);
			venues = venueRepository.findByIdGreaterThanOrderByIdAsc(decoded.id(), pageable);
		}
		
		boolean hasNext = venues.size() > limit;
		String nextCursor = null;
		
		List<Venue> page = venues;
		
		if(hasNext) {
			Venue lastVenue = venues.get(limit - 1);
			
			VenueCursor venueCursor = new VenueCursor(
					lastVenue.getId()
				);
			
			nextCursor =  VenueCursorParser.encode(venueCursor);
			
			page = venues.subList(0, limit);
		}
		
		List<VenueResponse> responses = page.stream()
			.map(venue -> toResponse(venue))
			.toList();
		
		return new CursorPage<>(
					responses,
					nextCursor,
					hasNext
				);
	}
	
	@Transactional(readOnly = true)
	public VenueResponse getVenue(UUID venueId) {
		Venue venue = findVenue(venueId);
		
		return toResponse(venue);
	}
	
	@Transactional
	public UUID createVenue(CreateVenueCommand command) {
		
		Venue venue = Venue.create(
				command.name(),
				command.address());
		
		venueRepository.save(venue);
		
		return venue.getUuid();
	}
	
	@Transactional
	public void updateVenue(UpdateVenueCommand command) {
		Venue venue = findVenue(command.id());
		
		venue.changeName(command.name());
		venue.changeAddress(command.address());
	}
	
	@Transactional
	public void deleteVenue(UUID venueId) {
		Venue venue = findVenue(venueId);
		
		venueRepository.delete(venue);
	}
	
	private Venue findVenue(UUID venueId) {
		return venueRepository.findByUuid(venueId)
				.orElseThrow(() -> new VenueNotFoundException());
	}
	
	private VenueResponse toResponse(Venue venue) {
		return new VenueResponse(
				venue.getUuid(),
				venue.getName(),
				venue.getAddress()
				);
	}
}

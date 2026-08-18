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
import com.ticket_booking.venue.responses.VenueResponses.VenueSummaryResponse;
import com.ticket_booking.venue.utils.VenueCursor;
import com.ticket_booking.venue.utils.VenueCursorParser;
import com.ticket_booking.venue.utils.VenueMapper;

@Service
public class VenueService {

	private final IVenueRepository venueRepository;
	
	public VenueService(IVenueRepository venueRepository) {
		this.venueRepository = venueRepository;
	}
	
	@Transactional(readOnly = true)
	public CursorPage<VenueSummaryResponse> getVenues(String cursor, int limit) {
		
		Pageable pageable = PageRequest.of(0, limit + 1);
		
		List<Venue> venues = Strings.isBlank(cursor)
		    		? venueRepository.findByOrderByIdAsc(pageable)
		    		: findNextPage(cursor, pageable);
		
		if(venues.isEmpty()) {
        	return new CursorPage<>(
        			List.of(),
        			null,
        			false
        	);
        }
		
		boolean hasNext = venues.size() > limit;
		
		List<Venue> page = hasNext 
				? venues.subList(0, limit)
				: venues;

		String nextCursor = hasNext
				? createCursor(page.getLast())
				: null;
		
		List<VenueSummaryResponse> responses = page.stream()
			.map(venue -> VenueMapper.toSummaryResponse(venue))
			.toList();
		
		return new CursorPage<>(
					responses,
					nextCursor,
					hasNext
		);
	}
	
	@Transactional(readOnly = true)
	public VenueResponse getVenue(UUID venueId) {
		Venue venue = venueRepository.findByUuidFetchSeats(venueId)
				.orElseThrow(() -> new VenueNotFoundException());
		
		return VenueMapper.toResponse(venue);
	}
	
	@Transactional
	public UUID createVenue(CreateVenueCommand command) {
		
		Venue venue = Venue.create(
				command.name(),
				command.address(),
				command.timeZone()
				);
		
		for(SeatDto seat : command.seats()) {
			venue.addSeat(
					seat.number(),
					seat.row()
				);
		}
		
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
	public void deactivateVenue(UUID venueId) {
		Venue venue = findVenue(venueId);
		venue.deactivate();
	}
	
	private Venue findVenue(UUID venueId) {
		return venueRepository.findByUuid(venueId)
				.orElseThrow(() -> new VenueNotFoundException());
	}
	
	private List<Venue> findNextPage(String cursor, Pageable pageable) {
		VenueCursor decoded = VenueCursorParser.decode(cursor);

		return venueRepository.findByIdGreaterThanOrderByIdAsc(
				decoded.venueId(), 
				pageable
			);
	}
	
	private String createCursor(Venue venue) {
		VenueCursor venueCursor = new VenueCursor(
				venue.getId()
			);
		
		return VenueCursorParser.encode(venueCursor);
	}
}

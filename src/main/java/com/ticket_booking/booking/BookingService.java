package com.ticket_booking.booking;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.commands.BookingCommands.CreateBookingCommand;
import com.ticket_booking.booking.exceptions.BookingNotFoundException;
import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.utils.BookingCursor;
import com.ticket_booking.booking.utils.BookingCursorParser;
import com.ticket_booking.booking.utils.BookingMapper;
import com.ticket_booking.common.CursorPage;
import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.BookingSeat;
import com.ticket_booking.domain.models.Event;
import com.ticket_booking.domain.models.EventSeat;
import com.ticket_booking.event.exceptions.EventNotFoundException;
import com.ticket_booking.event.exceptions.EventSeatNotFoundException;
import com.ticket_booking.event.repositories.IEventRepository;


@Service
public class BookingService {
	
	private final IBookingRepository bookingRepository;
	private final IEventRepository eventRepository;
	
	public BookingService(
			IBookingRepository bookingRepository,
			IEventRepository eventRepository) {
		this.bookingRepository = bookingRepository;
		this.eventRepository = eventRepository;
	}

	@Transactional(readOnly = true)
	public CursorPage<BookingResponse> getUserBookings(Long userId, String cursor, int limit) {

		Pageable pageable = PageRequest.of(0, limit + 1);

        List<Booking> bookings = Strings.isBlank(cursor)
        		? bookingRepository.findFirstPage(userId, pageable)
        		: findNextPage(userId, cursor, pageable);
        
        if(bookings.isEmpty()) {
        	return new CursorPage<>(
        			List.of(),
        			null,
        			false
        	);
        }

        boolean hasNext = bookings.size() > limit;
        
        List<Booking> page = hasNext
        		? bookings.subList(0, limit)
        		: bookings;
        
        String nextCursor = hasNext
        		? createCursor(page.getLast())
        		: null;
        
        List<Long> ids = page.stream()
                .map(booking -> booking.getId())
                .toList();
		
        Map<Long, List<BookingSeat>> seatsMap = bookingRepository
        		.fetchBookingsSeats(ids)
                .stream()
                .collect(Collectors.groupingBy(
                			bookingSeat -> bookingSeat.getBookingId()
                ));

        List<BookingResponse> responses = page.stream()
        		.map(booking -> BookingMapper.toResponse(
                        booking,
                        seatsMap.getOrDefault(booking.getId(), List.of())
                ))
                .toList();
		
		return new CursorPage<>(
				responses,
				nextCursor,
				hasNext
		);
	}
	
	@Transactional(readOnly = true)
	public BookingResponse getUserBooking(Long userId, UUID bookingId) {
		
		Booking booking = bookingRepository.findByUuidAndUserIdFull(bookingId, userId)
				.orElseThrow(() -> new BookingNotFoundException());
		
		
		return BookingMapper.toResponse(booking, booking.getBookingSeats());
	}
	
	@Transactional
	public UUID createBooking(CreateBookingCommand command) {
		
		Event event = eventRepository.findByUuid(command.eventId())
				.orElseThrow(() -> new EventNotFoundException());
		
		event.ensureBookable();

	    List<EventSeat> seats =
	            eventRepository.findSeatsForBooking(
	                    command.eventId(),
	                    command.seatIds());

	    if (seats.size() != command.seatIds().size()) {
	        throw new EventSeatNotFoundException();
	    }
		
		Booking booking = Booking.create(
				command.user(), 
				event, 
				seats
			);
		
		bookingRepository.save(booking);
		
		return booking.getUuid();
	}
	
	@Transactional
	public void cancelBooking(Long userId, UUID bookingId) {
		Booking booking = bookingRepository
				.findByUuidAndUserIdWithSeats(bookingId, userId)
				.orElseThrow(() -> new BookingNotFoundException());
		
		booking.cancel();
	}
	
	private List<Booking> findNextPage(
			Long userId, 
			String cursor, 
			Pageable pageable) {
		
		BookingCursor decoded = BookingCursorParser.decode(cursor);

        return bookingRepository.findNextPage(
                userId,
                decoded.bookingTimestamp(),
                decoded.bookingId(),
                pageable);
	}
	
	private String createCursor(Booking booking) {
		BookingCursor bookingCursor = new BookingCursor(
				booking.getBookedAt(),
				booking.getId()
			);
	
		return BookingCursorParser.encode(bookingCursor);
	}
}

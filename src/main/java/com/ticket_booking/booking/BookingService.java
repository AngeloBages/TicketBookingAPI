package com.ticket_booking.booking;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.commands.BookingCommands.CreateBookingCommand;
import com.ticket_booking.booking.exceptions.BookingNotFoundException;
import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.booking.responses.BookingResponses.BookingConfirmationResult;
import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.utils.BookingCursor;
import com.ticket_booking.booking.utils.BookingCursorParser;
import com.ticket_booking.booking.utils.BookingMapper;
import com.ticket_booking.common.CursorPage;
import com.ticket_booking.common.DomainEventDispatcher;
import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.BookingSeat;
import com.ticket_booking.domain.models.Event;
import com.ticket_booking.domain.models.EventSeat;
import com.ticket_booking.domain.models.enums.IdempotencyOperation;
import com.ticket_booking.event.EventService;
import com.ticket_booking.event.exceptions.EventNotFoundException;
import com.ticket_booking.event.exceptions.EventSeatNotFoundException;
import com.ticket_booking.event.repositories.IEventRepository;
import com.ticket_booking.idempotency.Idempotent;

import io.micrometer.observation.annotation.Observed;


@Service
public class BookingService {
	
	private final IBookingRepository bookingRepository;
	private final IEventRepository eventRepository;
	private final BookingProperties bookingProperties;
	private final EventService eventService;
	private final DomainEventDispatcher dispatcher;
	
	public BookingService(
			IBookingRepository bookingRepository,
			IEventRepository eventRepository,
			BookingProperties bookingProperties,
			EventService eventService,
			DomainEventDispatcher dispatcher) {
		this.bookingRepository = bookingRepository;
		this.eventRepository = eventRepository;
		this.bookingProperties = bookingProperties;
		this.eventService = eventService;
		this.dispatcher = dispatcher;
	}

	@Transactional(readOnly = true)
	public CursorPage<BookingResponse> getUserBookings(Long userId, String cursor, int limit) {

		Limit queryLimit = Limit.of(limit + 1);

        List<Booking> bookings = Strings.isBlank(cursor)
        		? bookingRepository.findFirstPage(userId, queryLimit)
        		: findNextPage(userId, cursor, queryLimit);
        
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
	
	@Observed(
        name = "booking.create",
        contextualName = "create-booking"
    )
	@Idempotent(
		operation = IdempotencyOperation.BOOKING_CREATE,
		key = "#command.idempotencyKey", 
		identityId = "#command.user.id",
		fingerprint = "{#command.eventId, #command.seatIds}"
	)
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
	    
	    Instant expiresAt = Instant.now().plus(
	    		Duration.ofMinutes(
	    				bookingProperties.expirationMin()
	    		));
		
		Booking booking = Booking.create(
				command.user(), 
				event, 
				expiresAt,
				seats
			);
		
		bookingRepository.save(booking);
		
		return booking.getUuid();
	}
	
	@Observed(
        name = "booking.cancel.user",
        contextualName = "cancel-booking-by-user"
    )
	@Transactional
	public void cancelBooking(Long userId, UUID bookingId) {
		Booking booking =  findBooking(
				bookingId, 
				userId
		);
		
		booking.cancel();
		
		eventService.releaseSeats(
				booking.getBookingSeatsIds()
		);
		
	    dispatcher.publishEventsFor(booking);
	}
	
	@Observed(
        name = "booking.cancel.pending",
        contextualName = "cancel-pending-booking"
    )
	@Transactional
	public void cancelBooking(Long bookingId) {
		Booking booking = findBookingForUpdate(bookingId);
		
		if (!booking.isPending()) {
	        return;
	    }
		
		booking.cancel();
		
		eventService.releaseSeats(
				booking.getBookingSeatsIds()
		);

	    dispatcher.publishEventsFor(booking);
	}
	
	@Observed(
        name = "booking.confirm",
        contextualName = "confirm-booking"
    )
	@Transactional
	public BookingConfirmationResult confirmBooking(Long bookingId) {
		Booking booking = findBookingForUpdate(bookingId);
		
		if(booking.isExpired(Instant.now()) ||
				booking.isCancelled()) {
			return BookingConfirmationResult.REFUND_REQUIRED;
		}
		
		booking.confirm();
		
		eventService.bookSeats(
				booking.getBookingSeatsIds()
		);
		
	    dispatcher.publishEventsFor(booking);
		
		return BookingConfirmationResult.BOOKING_CONFIRMED;
	}
	
	@Transactional
	public void handleSuccessfulRefund(Long bookingId) {
		Booking booking = findBookingForUpdate(bookingId);

		if(booking.isConfirmed()) {
			booking.markRefunded();
			
			eventService.releaseSeats(
					booking.getBookingSeatsIds()
			);
		}
	}
	
	@Observed(
	    name = "booking.expire.batch",
	    contextualName = "expire-pending-bookings"
	)
	@Transactional
	public int expirePendingBookings(Limit limit) {
	    Instant now = Instant.now();

	    List<Booking> bookings =
	        bookingRepository
	        	.findPendingBookingsExpiredForUpdate(now, limit);

	    for (Booking booking : bookings) {
	        booking.expire();
	        
	        eventService.releaseSeats(
					booking.getBookingSeatsIds()
			);

		    dispatcher.publishEventsFor(booking);
	    }
	    
	    return bookings.size();
	}
	
	private List<Booking> findNextPage(
			Long userId, 
			String cursor, 
			Limit limit) {
		
		BookingCursor decoded = BookingCursorParser.decode(cursor);

        return bookingRepository.findNextPage(
                userId,
                decoded.bookingTimestamp(),
                decoded.bookingId(),
                limit);
	}
	
	private String createCursor(Booking booking) {
		BookingCursor bookingCursor = new BookingCursor(
				booking.getBookedAt(),
				booking.getId()
			);
	
		return BookingCursorParser.encode(bookingCursor);
	}
	
	private Booking findBooking(
			UUID bookingId,
			Long userId) {
		
		return bookingRepository
				.findByUuidAndUserId(bookingId, userId)
				.orElseThrow(() -> new BookingNotFoundException());
	}
	
	private Booking findBookingForUpdate(Long bookingId) {
		return bookingRepository.findByIdForUpdate(bookingId)
				.orElseThrow(() -> new BookingNotFoundException());
	}
}

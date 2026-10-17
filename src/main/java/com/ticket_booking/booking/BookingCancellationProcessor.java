package com.ticket_booking.booking;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.domain.models.enums.BookingStatus;

@Component
public class BookingCancellationProcessor {
	
	private final IBookingRepository repository;
	private final BookingService bookingService;
	
	public BookingCancellationProcessor(
			IBookingRepository repository,
			BookingService bookingService) {
		this.repository = repository;
		this.bookingService = bookingService;
	}

	@Transactional
	public int cancelPendingBookingsBatch(Long eventId, int batchSize) {
		List<Long> bookingIds = repository
				.findIdByEventIdAndStatus(
						eventId,
						BookingStatus.PENDING,
						Limit.of(batchSize)
				);

	    for (Long bookingId : bookingIds) {
	       bookingService.cancelBooking(bookingId);
	    }
	    
	    return bookingIds.size();
	}
}

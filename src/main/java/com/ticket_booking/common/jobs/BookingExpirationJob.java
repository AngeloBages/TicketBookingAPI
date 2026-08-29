package com.ticket_booking.common.jobs;

import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticket_booking.booking.BookingService;

@Component
public class BookingExpirationJob {

	private final BookingService bookingService;
	
	public BookingExpirationJob(BookingService bookingService) {
		this.bookingService = bookingService;
	}
	
	@Scheduled(fixedRateString = "${app.booking.expiration.fixed-rate:5000}")
    public void expireBookings() {
        bookingService.expirePendingBookings(Limit.of(100));
    }
}

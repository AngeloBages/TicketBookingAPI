package com.ticket_booking.booking.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticket_booking.booking.BookingProperties;
import com.ticket_booking.booking.BookingService;

import io.micrometer.observation.annotation.Observed;

@Component
public class BookingExpirationJob {
	
	private static final Logger log =
            LoggerFactory.getLogger(BookingExpirationJob.class);

	private final BookingService bookingService;
	private BookingProperties properties;
	
	public BookingExpirationJob(
			BookingService bookingService,
			BookingProperties properties) {
		this.bookingService = bookingService;
		this.properties = properties;
	}
	
	@Observed(
        name = "job.booking.expiration",
        contextualName = "booking-expiration-job"
    )
	@Scheduled(fixedRateString = "${app.booking.expiration.fixed-rate:5000}")
    public void expireBookings() {
        int expired = bookingService.expirePendingBookings(
        		Limit.of(
        				properties.expiration().batchSize()
        		)
        );
        
        if (expired > 0) {
            log.info("Booking expiration batch completed expiredCount={}", expired);
        } else {
            log.debug("Booking expiration batch completed expiredCount=0");
        }
    }
}

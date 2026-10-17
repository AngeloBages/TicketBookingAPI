package com.ticket_booking.booking.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.domain.events.booking.BookingCanceledEvent;
import com.ticket_booking.domain.events.booking.BookingConfirmedEvent;
import com.ticket_booking.domain.events.booking.BookingExpiredEvent;

@Component
public class BookingOperationLogListener {

	private static final Logger log = 
			LoggerFactory.getLogger(BookingOperationLogListener.class);
	
	@TransactionalEventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        log.info(
                "Booking confirmed bookingId={}",
                event.bookingUuid()
        );
    }
	
	@TransactionalEventListener
    public void onBookingCanceled(BookingCanceledEvent event) {
        log.info(
                "Booking canceled bookingId={}",
                event.bookingUuid()
        );
    }
	
	@TransactionalEventListener
    public void onBookingExpired(BookingExpiredEvent event) {
        log.info(
                "Booking expired bookingId={}",
                event.bookingUuid()
        );
    }
}

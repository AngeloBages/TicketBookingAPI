package com.ticket_booking.booking.listeners;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.booking.BookingCancellationProcessor;
import com.ticket_booking.booking.BookingProperties;
import com.ticket_booking.domain.events.event.EventCanceledEvent;

@Component
public class EventCancellationListener {
	
	private final BookingCancellationProcessor bookingCancellationProcessor;
	private final BookingProperties bookingProperties;
	
	public EventCancellationListener(
			BookingCancellationProcessor bookingCancellationProcessor,
			BookingProperties bookingProperties) {
		this.bookingCancellationProcessor = bookingCancellationProcessor;
		this.bookingProperties = bookingProperties;
	}

	@Async
	@TransactionalEventListener()
	public void on(EventCanceledEvent event) {
		
		int processed;

        do {
            processed =
            		bookingCancellationProcessor
            				.cancelPendingBookingsBatch(
		            				event.id(),
		            				bookingProperties
		            					.cancellation()
		            					.batchSize()
		            		);
        } while (processed > 0);
	}
}

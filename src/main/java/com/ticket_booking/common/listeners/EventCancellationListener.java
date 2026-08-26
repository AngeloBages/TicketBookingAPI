package com.ticket_booking.common.listeners;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.booking.BookingService;
import com.ticket_booking.domain.events.EventCancelledEvent;

@Component
public class EventCancellationListener {
	
	private final BookingService bookingService;
	
	public EventCancellationListener(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@Async
	@TransactionalEventListener()
	public void on(EventCancelledEvent event) {
		bookingService.handleEventCancellation(event.id());
	}
}

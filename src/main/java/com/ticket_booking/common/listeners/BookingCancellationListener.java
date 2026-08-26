package com.ticket_booking.common.listeners;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.domain.events.BookingCancelledEvent;
import com.ticket_booking.event.EventService;

@Component
public class BookingCancellationListener {

	private final EventService eventService;
	
	public BookingCancellationListener(
			EventService eventService) {
		this.eventService = eventService;
	}
	
	@Async
	@TransactionalEventListener
	public void on(BookingCancelledEvent event) {
		eventService.handleBookingCancellation(event.bookingSeatIds());
	}
}

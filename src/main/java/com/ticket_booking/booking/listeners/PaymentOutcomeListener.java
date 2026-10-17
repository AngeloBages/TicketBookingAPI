package com.ticket_booking.booking.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ticket_booking.booking.BookingService;
import com.ticket_booking.booking.responses.BookingResponses.BookingConfirmationResult;
import com.ticket_booking.domain.events.payment.PaymentCapturedEvent;
import com.ticket_booking.domain.events.payment.PaymentRefundedEvent;

@Component
public class PaymentOutcomeListener {

	private final BookingService bookingService;
	
	public PaymentOutcomeListener(
			BookingService bookingService) {
		this.bookingService = bookingService;
	}
	
	@EventListener
	public void on(PaymentCapturedEvent event) {
		BookingConfirmationResult result = 
				bookingService.confirmBooking(event.bookingId());

		if(result.requiresRefund()) {
			bookingService.requestBookingRefund(event.paymentId());
		}
	}
	
	@EventListener
	public void on(PaymentRefundedEvent event) {
		bookingService.handleSuccessfulRefund(event.bookingId());
	}
}

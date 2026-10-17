package com.ticket_booking.booking.exceptions;

public class BookingAlreadyExpiredException extends InvalidBookingStateException {

	private static final long serialVersionUID = 1L;
	
	public BookingAlreadyExpiredException() {
		super("Booking is already expired");
	}
}

package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class BookingNotFoundException extends DomainException {

	private static final long serialVersionUID = -2757094049844671087L;

	public BookingNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "Booking Not Found",
            "Booking was not found."
        );
    }
}

package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidBookingStateException extends DomainException {

	private static final long serialVersionUID = -6440223456930916878L;

	public InvalidBookingStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Booking State",
            message
        );
    }
}


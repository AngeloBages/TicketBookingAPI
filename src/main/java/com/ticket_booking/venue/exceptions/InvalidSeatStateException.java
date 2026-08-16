package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidSeatStateException extends DomainException {

	private static final long serialVersionUID = 8378349072118343422L;

	public InvalidSeatStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Seat State",
            message
        );
    }
}

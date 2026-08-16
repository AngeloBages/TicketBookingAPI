package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidEventSeatStateException extends DomainException {

	private static final long serialVersionUID = 5752558957850289530L;

	public InvalidEventSeatStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Seat State",
            message
        );
    }
}

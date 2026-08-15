package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidVenueStateException extends DomainException {

	private static final long serialVersionUID = 3195120915770376892L;

	public InvalidVenueStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Venue State",
            message
        );
    }
}

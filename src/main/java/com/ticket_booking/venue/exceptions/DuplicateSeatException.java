package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class DuplicateSeatException extends DomainException {

	private static final long serialVersionUID = -3129172761974418480L;

	public DuplicateSeatException(int number, String row) {
        super(
            HttpStatus.CONFLICT,
            "Duplicate Seat",
            "Seat %d in row '%s' already exists in the venue."
                .formatted(number, row)
        );
    }
}

package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class DuplicateEventSeatException extends DomainException {

	private static final long serialVersionUID = -3024893744655262353L;

	public DuplicateEventSeatException(int number, String row) {
        super(
            HttpStatus.CONFLICT,
            "Duplicate Seat",
            "Seat %d in row '%s' already exists for this event."
                .formatted(number, row)
        );
    }
}

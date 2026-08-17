package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class EventSeatNotAvailableException extends DomainException {

	private static final long serialVersionUID = -3534519245174162030L;

	public EventSeatNotAvailableException() {
        super(
            HttpStatus.CONFLICT,
            "Event seat not available",
            "Seat is not available for booking."
        );
    }
}
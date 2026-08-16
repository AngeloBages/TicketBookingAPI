package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidEventStateException extends DomainException {

	private static final long serialVersionUID = 1139459312627527452L;

	public InvalidEventStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Event State",
            message
        );
    }
}

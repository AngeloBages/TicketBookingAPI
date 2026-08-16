package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class EventNotFoundException extends DomainException {

	private static final long serialVersionUID = -6180448276253832709L;

	public EventNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "Event Not Found",
            "Event was not found."
        );
    }
	
	public EventNotFoundException(String role) {
        super(
            HttpStatus.NOT_FOUND,
            "Event Not Found",
            "Event was not found: " + role
        );
    }
}

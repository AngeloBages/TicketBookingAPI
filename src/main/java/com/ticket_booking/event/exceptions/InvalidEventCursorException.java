package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidEventCursorException extends DomainException {
	
	private static final long serialVersionUID = 1831181375465273887L;

	public InvalidEventCursorException() {
		super(
				HttpStatus.BAD_REQUEST,
				"Invalid event cursor",
				"The provided event cursor is invalid."
				);
	}
}

package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidEventFieldException extends DomainException {
	
	private static final long serialVersionUID = -729102220828218542L;

	public InvalidEventFieldException(
			String field,
			String reason) {

		super(
				HttpStatus.BAD_REQUEST,
				"Invalid event",
				"Field '" + field + "' " + reason + "."
				);
	}
}
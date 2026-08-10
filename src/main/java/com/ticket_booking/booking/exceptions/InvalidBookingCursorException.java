package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidBookingCursorException extends DomainException {
	
	private static final long serialVersionUID = -2059343706921806665L;

	public InvalidBookingCursorException() {
		super(
				HttpStatus.BAD_REQUEST,
				"Invalid booking cursor",
				"The provided booking cursor is invalid."
				);
	}
}

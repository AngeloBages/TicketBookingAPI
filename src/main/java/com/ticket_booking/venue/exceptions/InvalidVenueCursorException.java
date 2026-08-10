package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public final class InvalidVenueCursorException extends DomainException {

	private static final long serialVersionUID = -5562338025674661742L;

	public InvalidVenueCursorException() {
		super(
				HttpStatus.BAD_REQUEST,
				"Invalid venue cursor",
				"The provided venue cursor is invalid."
				);
	}
}

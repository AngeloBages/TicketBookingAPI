package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public final class InvalidVenueFieldException extends DomainException {

	private static final long serialVersionUID = -4745392301318731372L;

	public InvalidVenueFieldException(
			String field,
			String reason) {

		super(
				HttpStatus.BAD_REQUEST,
				"Invalid venue",
				"Field '" + field + "' " + reason + "."
				);
	}
}

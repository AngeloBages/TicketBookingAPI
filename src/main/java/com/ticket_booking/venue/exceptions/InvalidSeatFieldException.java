package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidSeatFieldException extends DomainException {

	private static final long serialVersionUID = 869197070487968041L;

	public InvalidSeatFieldException(
			String field,
			String reason) {

		super(
				HttpStatus.BAD_REQUEST,
				"Invalid seat",
				"Field '" + field + "' " + reason + "."
				);
	}
}
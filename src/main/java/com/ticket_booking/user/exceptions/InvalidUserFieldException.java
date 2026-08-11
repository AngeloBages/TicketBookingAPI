package com.ticket_booking.user.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public final class InvalidUserFieldException extends DomainException {

	private static final long serialVersionUID = -6539095792625487436L;

	public InvalidUserFieldException(
			String field,
			String reason) {

		super(
				HttpStatus.BAD_REQUEST,
				"Invalid venue",
				"Field '" + field + "' " + reason + "."
				);
	}
}

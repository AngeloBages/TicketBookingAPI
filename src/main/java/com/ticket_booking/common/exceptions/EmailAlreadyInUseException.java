package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public class EmailAlreadyInUseException extends DomainException {

	private static final long serialVersionUID = 2586876346076701276L;
	
	public EmailAlreadyInUseException() {
		super(
				HttpStatus.CONFLICT,
				"Email already in use",
				"Email is already in use.");
	}

	public EmailAlreadyInUseException(String email) {
		super(
				HttpStatus.CONFLICT,
				"Email already in use",
				"Email is already in use: " + email);
	}
}

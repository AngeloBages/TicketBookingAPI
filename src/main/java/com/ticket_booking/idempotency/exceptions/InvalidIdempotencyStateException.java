package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidIdempotencyStateException extends DomainException {

private static final long serialVersionUID = 1L;

	public InvalidIdempotencyStateException(
	    String message) {
	
		super(
		        HttpStatus.CONFLICT,
		        "Invalid Idempotency State",
		        message
		);
	}
}

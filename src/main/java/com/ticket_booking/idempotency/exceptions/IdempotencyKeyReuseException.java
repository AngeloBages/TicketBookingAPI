package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class IdempotencyKeyReuseException extends DomainException {

	private static final long serialVersionUID = 1L;

	public IdempotencyKeyReuseException() {
		super(
		    HttpStatus.CONFLICT,
		    "Idempotency Key Reuse",
		    "The idempotency key was already used with a different request."
		);
	}
}
package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class IdempotencyRecordNotFoundException extends DomainException {

	private static final long serialVersionUID = 1L;

	public IdempotencyRecordNotFoundException() {
		super(
		    HttpStatus.INTERNAL_SERVER_ERROR,
		    "Idempotency Record Not Found",
		    "The idempotency record could not be found."
		);
	}
}

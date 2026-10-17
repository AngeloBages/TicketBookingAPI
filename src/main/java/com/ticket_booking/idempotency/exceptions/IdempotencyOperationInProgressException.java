package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class IdempotencyOperationInProgressException extends DomainException {

	private static final long serialVersionUID = 1L;

	public IdempotencyOperationInProgressException() {
		super(
		    HttpStatus.CONFLICT,
		    "Operation In Progress",
		    "An operation with this idempotency key is already in progress."
		);
	}
}

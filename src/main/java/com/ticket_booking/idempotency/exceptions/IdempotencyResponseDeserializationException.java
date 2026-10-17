package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class IdempotencyResponseDeserializationException extends DomainException {

	private static final long serialVersionUID = 1L;

	public IdempotencyResponseDeserializationException() {
		super(
		    HttpStatus.CONFLICT,
		    "Idempotency Respose Deserialization Failed",
		    "Could not deserialize idempotent response"
		);
	}
}

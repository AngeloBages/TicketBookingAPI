package com.ticket_booking.idempotency.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class IdempotencyFingerprintException extends DomainException {

	private static final long serialVersionUID = 1L;

	public IdempotencyFingerprintException() {
		super(
		    HttpStatus.CONFLICT,
		    "Idempotency Fingerprint Failed",
		    "Could not generate idempotency request fingerprint"
		);
	}
}

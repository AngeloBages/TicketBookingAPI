package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidPaymentFieldException extends DomainException {
	
	private static final long serialVersionUID = 1L;

	public InvalidPaymentFieldException(
			String field,
			String reason) {

		super(
				HttpStatus.BAD_REQUEST,
				"Invalid payment",
				"Field '" + field + "' " + reason + "."
				);
	}
}

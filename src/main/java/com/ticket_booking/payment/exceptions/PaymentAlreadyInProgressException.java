package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentAlreadyInProgressException extends DomainException {

	private static final long serialVersionUID = 1L;

	public PaymentAlreadyInProgressException() {
		super(
		    HttpStatus.CONFLICT,
		    "Payment In Progress",
		    "The payment for this booking is already in progress."
		);
	}
}
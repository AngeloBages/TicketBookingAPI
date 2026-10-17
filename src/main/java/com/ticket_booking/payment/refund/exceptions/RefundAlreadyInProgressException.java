package com.ticket_booking.payment.refund.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class RefundAlreadyInProgressException extends DomainException {

	private static final long serialVersionUID = 1L;

	public RefundAlreadyInProgressException() {
		super(
		    HttpStatus.CONFLICT,
		    "Refund In Progress",
		    "The refund for this payment is already in progress."
		);
	}
}
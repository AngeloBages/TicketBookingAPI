package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentNotFoundException extends DomainException{

	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException() {
		super(
	            HttpStatus.NOT_FOUND,
	            "Payment Not Found",
	            "Payment was not found."
	        );
	}
}
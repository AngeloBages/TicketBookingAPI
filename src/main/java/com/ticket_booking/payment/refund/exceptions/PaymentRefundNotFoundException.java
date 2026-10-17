package com.ticket_booking.payment.refund.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentRefundNotFoundException extends DomainException{

	private static final long serialVersionUID = 1L;

	public PaymentRefundNotFoundException() {
		super(
	            HttpStatus.NOT_FOUND,
	            "Payment Refund Not Found",
	            "Payment refund was not found."
	        );
	}
}

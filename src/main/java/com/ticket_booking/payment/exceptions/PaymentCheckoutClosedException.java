package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentCheckoutClosedException extends DomainException {

	private static final long serialVersionUID = 1L;

	public PaymentCheckoutClosedException() {
        super(
            HttpStatus.CONFLICT,
            "Payment Checkout Session Closed",
            "The payment checkout session has closed"
        );
    }
}
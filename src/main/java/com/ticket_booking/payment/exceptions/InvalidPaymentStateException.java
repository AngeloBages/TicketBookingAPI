package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidPaymentStateException extends DomainException {

	private static final long serialVersionUID = 1L;

	public InvalidPaymentStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Payment State",
            message
        );
    }
}

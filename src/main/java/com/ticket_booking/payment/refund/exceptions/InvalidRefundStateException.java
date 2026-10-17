package com.ticket_booking.payment.refund.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidRefundStateException extends DomainException {

	private static final long serialVersionUID = 1L;

	public InvalidRefundStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Refund State",
            message
        );
    }
}

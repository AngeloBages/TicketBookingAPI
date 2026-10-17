package com.ticket_booking.payment.webhook.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidPaymentWebhookStateException extends DomainException {

	private static final long serialVersionUID = 1L;

	public InvalidPaymentWebhookStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Payment Webhook State",
            message
        );
    }
}

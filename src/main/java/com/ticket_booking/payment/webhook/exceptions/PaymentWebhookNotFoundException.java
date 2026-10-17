package com.ticket_booking.payment.webhook.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentWebhookNotFoundException extends DomainException{

	private static final long serialVersionUID = 1L;

	public PaymentWebhookNotFoundException() {
		super(
	            HttpStatus.NOT_FOUND,
	            "Payment Webhook Not Found",
	            "Payment webhook was not found."
	        );
	}
	
	public PaymentWebhookNotFoundException(
			Long webhookId) {
		super(
	            HttpStatus.NOT_FOUND,
	            "Payment Webhook Not Found",
	            "Payment webhook was not found: " + webhookId
	        );
	}
}

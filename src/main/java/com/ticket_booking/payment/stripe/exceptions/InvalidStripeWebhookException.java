package com.ticket_booking.payment.stripe.exceptions;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.enums.PaymentProviderType;

public class InvalidStripeWebhookException extends IntegrationException {

	private static final long serialVersionUID = 1L;

	public InvalidStripeWebhookException(
            String errorCode,
            IntegrationFailureType failureType,
            OperationOutcome outcome,
            Integer providerStatusCode,
            String message,
            Throwable cause) {

        super(
        		PaymentProviderType.STRIPE.toString(),
                errorCode,
                failureType,
                outcome,
                providerStatusCode,
                message,
                cause
        );
    }
	
	public InvalidStripeWebhookException(
            IntegrationFailureType failureType,
            OperationOutcome outcome,
            String message,
            Throwable cause) {

        super(
        		PaymentProviderType.STRIPE.toString(),
                "",
                failureType,
                outcome,
                401,
                message,
                cause
        );
    }
}

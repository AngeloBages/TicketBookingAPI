package com.ticket_booking.payment.stripe.exceptions;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.enums.PaymentProviderType;

public class InvalidStripeCheckoutStatusException  extends IntegrationException {

	private static final long serialVersionUID = 1L;

	public InvalidStripeCheckoutStatusException(String message) {

        super(
        		PaymentProviderType.STRIPE.toString(),
                "",
                IntegrationFailureType.PROVIDER_ERROR,
                OperationOutcome.FAILED,
                401,
                message,
                null
        );
    }
}

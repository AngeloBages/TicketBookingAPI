package com.ticket_booking.payment.exceptions;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;

public class PaymentProviderException extends IntegrationException {

	private static final long serialVersionUID = 1L;

	public PaymentProviderException(
            String serviceName,
            String errorCode,
            IntegrationFailureType failureType,
            OperationOutcome outcome,
            Integer providerStatusCode,
            String message,
            Throwable cause) {

        super(
                serviceName,
                errorCode,
                failureType,
                outcome,
                providerStatusCode,
                message,
                cause
        );
    }
}
	
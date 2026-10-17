package com.ticket_booking.payment.exceptions;

import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;

public class ExternalResourceNotFoundException extends PaymentProviderException{

	private static final long serialVersionUID = 1L;
	
	public ExternalResourceNotFoundException(
            String serviceName,
            String resourceName,
            String message,
            Throwable cause) {

        super(
                serviceName,
                resourceName.toLowerCase().replace(' ', '_') + "_not_found",
                IntegrationFailureType.NOT_FOUND,
                OperationOutcome.FAILED,
                404,
                message,
                cause
        );
    }
}

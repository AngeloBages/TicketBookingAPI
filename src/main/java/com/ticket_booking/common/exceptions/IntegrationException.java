package com.ticket_booking.common.exceptions;

import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;

public abstract class IntegrationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final String serviceName;
    private final String errorCode;
    private final IntegrationFailureType failureType;
    private final OperationOutcome outcome;
    private final Integer providerStatusCode;

    protected IntegrationException(
            String serviceName,
            String errorCode,
            IntegrationFailureType failureType,
            OperationOutcome outcome,
            Integer providerStatusCode,
            String message,
            Throwable cause) {

        super(message, cause);

        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.failureType = failureType;
        this.outcome = outcome;
        this.providerStatusCode = providerStatusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public IntegrationFailureType getFailureType() {
        return failureType;
    }

    public OperationOutcome getOutcome() {
        return outcome;
    }

	public Integer getProviderStatusCode() {
		return providerStatusCode;
	}
}


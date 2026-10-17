package com.ticket_booking.common.exceptions;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;

public final class IntegrationExceptionUtils {

	private IntegrationExceptionUtils() {}
	
	public static String getSafeFailureMessage(
            IntegrationFailureType failureType,
            OperationOutcome operationOutcome,
            String serviceName) {
		
		Objects.requireNonNull(failureType, "failureType must not be null");
		Objects.requireNonNull(failureType, "operationOutcome must not be null");
        Objects.requireNonNull(serviceName, "serviceName must not be null");

        boolean isOutcomeUnknown =
                operationOutcome == OperationOutcome.UNKNOWN;

        return switch (failureType) {
            case INVALID_REQUEST ->
                    "The request sent to the " + serviceName
                            + " service could not be accepted.";

            case RATE_LIMITED ->
                    "The " + serviceName
                            + " service is temporarily rate-limiting requests.";

            case PROVIDER_ERROR ->
                    isOutcomeUnknown
                            ? "The result of the request to the "
                                    + serviceName + " service could not be confirmed."
                            : "The " + serviceName
                                    + " service encountered an error.";

            case TRANSPORT_ERROR, TIMEOUT ->
                    isOutcomeUnknown
                            ? "The result of the request to the "
                                    + serviceName + " service could not be confirmed."
                            : "The " + serviceName
                                    + " service could not be reached.";

            case AUTHENTICATION, AUTHORIZATION ->
                    "The " + serviceName
                            + " integration could not authorize the request.";

            case NOT_FOUND ->
                    "The requested resource was not found in the "
                            + serviceName + " service.";

            case PAYMENT_FAILED ->
                    "The payment could not be completed.";

            case UNKNOWN ->
                    isOutcomeUnknown
                            ? "The result of the request to the "
                                    + serviceName + " service could not be confirmed."
                            : "An unexpected error occurred while communicating "
                                    + "with the " + serviceName + " service.";
        };
    }
	
	public static void logFailure(
			Logger log,
			String message,
			IntegrationException ex,
			Object...logArgs) {

        if (ex.getOutcome() == OperationOutcome.UNKNOWN) {
            log.warn(
            		message, 
            		appendThrowable(logArgs, ex)
            );
            return;
        }

        switch (ex.getFailureType()) {
            case PAYMENT_FAILED, INVALID_REQUEST, NOT_FOUND ->
                    log.info(
                    		message, 
                    		appendThrowable(logArgs, ex)
                    );

            case RATE_LIMITED, TRANSPORT_ERROR, TIMEOUT, PROVIDER_ERROR, UNKNOWN ->
                    log.warn(
                    		message, 
                    		appendThrowable(logArgs, ex)
                    );

            case AUTHENTICATION, AUTHORIZATION ->
                    log.error(
                    		message, 
                    		appendThrowable(logArgs, ex)
                    );
        }
    }
	
	private static Object[] appendThrowable(
			Object[] args, 
			Throwable ex) {
		
		return Stream.concat(
					Arrays.stream(args), 
					Stream.of(ex)
				)
				.toArray();
	}
}

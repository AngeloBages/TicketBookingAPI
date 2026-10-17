package com.ticket_booking.payment.stripe.utils;

import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.exceptions.PaymentProviderException;

public final class StripeExceptionTranslator {
	
	private StripeExceptionTranslator() {}
	
	public static  PaymentProviderException translate(
	        StripeException ex) {

		IntegrationFailureClassification failure = classify(ex);
		
		String serviceName = PaymentProviderType.STRIPE.getName();

	    return new PaymentProviderException(
	            serviceName,
	            ex.getCode(),
	            failure.type,
	            failure.outcome,
	            ex.getStatusCode(),
	            ex.getMessage(),
	            ex
	    );
	}
	
	private static IntegrationFailureClassification classify(StripeException ex) {
		
		if (ex instanceof ApiConnectionException) {
	        return new IntegrationFailureClassification(
	                IntegrationFailureType.TRANSPORT_ERROR,
	                OperationOutcome.UNKNOWN
	        );
	    }
		
		if (ex instanceof CardException) {
		    return new IntegrationFailureClassification(
		            IntegrationFailureType.PAYMENT_FAILED,
		            OperationOutcome.FAILED
		    );
		}

	    Integer statusCode = ex.getStatusCode();
	    
	    if (statusCode == null) {
	        return new IntegrationFailureClassification(
	                IntegrationFailureType.UNKNOWN,
	                OperationOutcome.UNKNOWN
	        );
	    }

	    return switch (statusCode) {
	    
		    case 408 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.TIMEOUT, 
		    			OperationOutcome.UNKNOWN
		    	);
		    	
		    case 429 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.RATE_LIMITED, 
		    			OperationOutcome.UNKNOWN
		    	);
		    	
		    case 401 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.AUTHENTICATION, 
		    			OperationOutcome.FAILED
		    	);
		    	
		    case 403 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.AUTHORIZATION, 
		    			OperationOutcome.FAILED
		    	);
		    	
		    case 404 ->
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.NOT_FOUND, 
		    			OperationOutcome.FAILED
		    	);
		    	
		    case 400, 422 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.INVALID_REQUEST, 
		    			OperationOutcome.FAILED
		    	);
		    	
		    case Integer code when code >= 500 -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.PROVIDER_ERROR, 
		    			OperationOutcome.UNKNOWN
		    	);
		    	
		    default -> 
		    	new IntegrationFailureClassification(
		    			IntegrationFailureType.UNKNOWN, 
		    			OperationOutcome.UNKNOWN
		    	);
		};
	}
	
	public record IntegrationFailureClassification(
	        IntegrationFailureType type,
	        OperationOutcome outcome
	) {}
}
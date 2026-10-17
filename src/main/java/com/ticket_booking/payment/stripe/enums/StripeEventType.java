package com.ticket_booking.payment.stripe.enums;

import static com.ticket_booking.payment.stripe.enums.StripeEventOperation.*;

import java.util.Arrays;
import java.util.Optional;

public enum StripeEventType {

	CHECKOUT_SESSION_COMPLETED(
            "checkout.session.completed",
			PAYMENT
    ),
    
    CHECKOUT_SESSION_EXPIRED(
    		"checkout.session.expired",
    		PAYMENT
    ),
    
    CHECKOUT_SESSION_ASYNC_PAYMENT_SUCCEEDED(
    		"checkout.session.async_payment_succeeded",
    		PAYMENT
    ),
    
    CHECKOUT_SESSION_ASYNC_PAYMENT_FAILED(
    		"checkout.session.async_payment_failed",
    		PAYMENT
    ),

    PAYMENT_INTENT_SUCCEEDED(
            "payment_intent.succeeded",
    		PAYMENT
    ),

    PAYMENT_INTENT_PAYMENT_FAILED(
            "payment_intent.payment_failed",
    		PAYMENT
    ),
    
    PAYMENT_INTENT_CANCELED(
    		"payment_intent.canceled",
    		PAYMENT
    ),
    
    PAYMENT_INTENT_REQUIRES_ACTION(
    		"payment_intent.requires_action",
    		PAYMENT
    ),
    
    /*REFUND_CREATED(
    		"refund.created",
    		REFUND
    ),*/
    
    REFUND_UPDATED(
      		"refund.updated",
			REFUND
	),
    
    REFUND_FAILED(
    		"refund.failed",
    		REFUND
    );
	
	//CHARGE_REFUNDED("charge.refunded");
	
    private final String value;
	private final StripeEventOperation operation;

    StripeEventType(
    		String value, 
    		StripeEventOperation operation) {
        this.value = value;
        this.operation = operation;
    }

    public static Optional<StripeEventType> from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst();
    }

    public String value() {
        return value;
    }
    
    public StripeEventOperation operation() {
    	return operation;
    }
}

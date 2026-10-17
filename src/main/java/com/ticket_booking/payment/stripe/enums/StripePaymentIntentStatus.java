package com.ticket_booking.payment.stripe.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StripePaymentIntentStatus {
	
	REQUIRES_PAYMENT_METHOD(
			"requires_payment_method"
	),
	
	REQUIRES_CONFIRMATION(
			"requires_confirmation"
	),
	
	REQUIRES_ACTION(
			"requires_action"
	),
	
	PROCESSING(
			"processing"
	),
	
	REQUIRES_CAPTURE(
			"requires_capture"
	),
	
	CANCELED(
			"canceled"
	),
	
	SUCCEEDED(
			"succeeded"
	);
	    
	private final String value;

	StripePaymentIntentStatus(String value) {
	 	this.value = value;
	}

	public static Optional<StripePaymentIntentStatus> from(String value) {
		return Arrays.stream(values())
				.filter(type -> type.value.equals(value))
				.findFirst();
	}

	public String value() {
		return value;
	}
}

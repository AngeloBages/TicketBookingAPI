package com.ticket_booking.payment.stripe.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StripeRefundStatus {
	
	PENDING(
			"pending"
	),
	
	REQUIRES_ACTION(
			"requires_action"
	),
	
	SUCCEEDED(
			"succeeded"
	),
	
	FAILED(
			"failed"
	),
	
	CANCELED(
			"canceled"
	);
	    
	private final String value;

	StripeRefundStatus(String value) {
	 	this.value = value;
	}

	public static Optional<StripeRefundStatus> from(String value) {
		return Arrays.stream(values())
				.filter(type -> type.value.equals(value))
				.findFirst();
	}

	public String value() {
		return value;
	}
}

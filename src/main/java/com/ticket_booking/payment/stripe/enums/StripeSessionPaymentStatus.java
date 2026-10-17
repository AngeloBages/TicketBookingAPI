package com.ticket_booking.payment.stripe.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StripeSessionPaymentStatus {
	
	PAID("paid"),

	UNPAID("unpaid"),
	
	NO_PAYMENT_REQUIRED("no_payment_required");
	    
	private final String value;

	StripeSessionPaymentStatus(String value) {
	 	this.value = value;
	}

	public static Optional<StripeSessionPaymentStatus> from(String value) {
		return Arrays.stream(values())
				.filter(type -> type.value.equals(value))
				.findFirst();
	}

	public String value() {
		return value;
	}
}

package com.ticket_booking.payment.stripe.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StripeSessionStatus {
	
	OPEN("open"),

	COMPLETE("complete"),
	
	EXPIRED("expired");
	    
	private final String value;

	StripeSessionStatus(String value) {
	 	this.value = value;
	}

	public static Optional<StripeSessionStatus> from(String value) {
		return Arrays.stream(values())
				.filter(type -> type.value.equals(value))
				.findFirst();
	}

	public String value() {
		return value;
	}
}
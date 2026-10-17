package com.ticket_booking.domain.models.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PaymentProviderType {
		STRIPE("Stripe");
	
	private final String name;
	
	private PaymentProviderType(String name) {
		this.name = name;
	}
	
	public static Optional<PaymentProviderType> from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.toString().equals(value.toUpperCase()))
                .findFirst();
    }
	
	public String getName() {
		return this.name;
	}
}

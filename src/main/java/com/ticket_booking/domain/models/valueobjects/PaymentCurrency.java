package com.ticket_booking.domain.models.valueobjects;

import java.util.Locale;
import java.util.Objects;

import com.ticket_booking.payment.exceptions.InvalidPaymentFieldException;

public record PaymentCurrency(String value) {

	public PaymentCurrency {
		String normalizedCurrency =
				Objects.requireNonNull(value)
				.toUpperCase(Locale.ROOT);

		if (normalizedCurrency.length() != 3) {
			throw new InvalidPaymentFieldException(
					"currency",
					"must be a 3-letter ISO currency code"
			);
		}

		value = normalizedCurrency;
	}
}

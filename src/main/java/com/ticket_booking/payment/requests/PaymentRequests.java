package com.ticket_booking.payment.requests;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public final class PaymentRequests {
	
	private PaymentRequests() {}

	public record CreatePaymentRequest(
				@NotNull
				UUID bookingId
	) {}
	
}

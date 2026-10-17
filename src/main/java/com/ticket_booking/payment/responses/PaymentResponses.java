package com.ticket_booking.payment.responses;

import java.time.Instant;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.domain.models.enums.RefundStatus;

public final class PaymentResponses{
	
	private PaymentResponses() {}

	public record CreatePaymentResponse(
			UUID paymentUuid,
			String returnUrl
	) {}
	
	public record GetPaymentResponse(
			UUID paymentUuid,
	        UUID bookingUuid,
	        long amount,
	        String currency,
	        PaymentStatus status,
	        String provider,
	        String providerPaymentId,
	        boolean requiresAction,
	        Instant createdAt,
	        Instant updatedAt
	) {}
	
	public record RefundResponse(
			UUID refundUuid,
			RefundStatus status
	) {}
}

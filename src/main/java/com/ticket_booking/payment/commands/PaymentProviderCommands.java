package com.ticket_booking.payment.commands;

import java.math.BigDecimal;
import java.util.UUID;

public final class PaymentProviderCommands {

	private PaymentProviderCommands() {}
	
	public record CreateProviderPaymentCommand(
			UUID paymentUuid,
			UUID bookingUuid,
			BigDecimal amount,
			String currency,
			String productName,
			String productDescription,
		    String idempotencyKey
	) {}
	
	public record RefundProviderPaymentCommand(
	        UUID refundUuid,
	        UUID paymentUuid,
	        String providerPaymentId,
	        BigDecimal amount,
	        String idempotencyKey
	) {}
	
	public record RetrieveProviderPaymentStatusCommand(
			UUID paymentUuid,
	        String providerPaymentId,
	        String providerCheckoutSessionId
	) {}
	
	public record RetrieveProviderRefundStatusCommand(
			UUID refundUuid,
			String providerRefundId,
			String providerPaymentId
	) {}
	
	public record CancelProviderPaymentCommand(
	        UUID paymentUuid,
	        String providerPaymentId
	) {}
}

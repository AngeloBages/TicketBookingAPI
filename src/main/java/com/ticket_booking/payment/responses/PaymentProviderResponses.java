package com.ticket_booking.payment.responses;

import com.ticket_booking.payment.ProviderPaymentStatus;
import com.ticket_booking.payment.refund.ProviderRefundStatus;

public class PaymentProviderResponses {
	
	private PaymentProviderResponses() {}

	public record CreatePaymentResult(
			String checkoutSessionId,
		    String redirectUrl
	) {}
	
	public record RefundPaymentResult(
	        String providerRefundId,
	        ProviderRefundStatus status,
	        String failureCode,
	        String failureMessage
	) {}

	public record PaymentStatusResult(
			ProviderPaymentStatus status,
	        String failureCode,
	        String failureMessage
	) {}
	
	public record RefundStatusResult(
			ProviderRefundStatus status,
	        String failureCode,
	        String failureMessage
	) {}
}

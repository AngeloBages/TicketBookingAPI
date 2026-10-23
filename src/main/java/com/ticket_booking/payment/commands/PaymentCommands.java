package com.ticket_booking.payment.commands;

import java.util.UUID;

public class PaymentCommands {

	public record CreatePaymentCommand(
				Long userId,
				UUID bookingId
	) {}
	
	public record CapturePaymentCommand(
				String idempotencyKey
	) {}
	
	public record RefundPaymentCommand(
				Long userId,
				UUID bookingId
	) {}
	
	public record ResumeCheckoutCommand(
				Long userId,
				UUID paymentId
	) {}
}

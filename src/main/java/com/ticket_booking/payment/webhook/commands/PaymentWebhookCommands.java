package com.ticket_booking.payment.webhook.commands;

import com.ticket_booking.domain.models.enums.PaymentProviderType;

public final class PaymentWebhookCommands {

	private PaymentWebhookCommands() {}
	
	public record ReceiveWebhookCommand(
            PaymentProviderType provider,
            String providerEventId,
            String type,
            String payload
	) {}
}

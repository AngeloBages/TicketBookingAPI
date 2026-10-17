package com.ticket_booking.payment.webhook;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.webhook")
public record PaymentWebhookProperties(
		Duration staleAfter,
		Processing processing
) {
	public record Processing(
			int batchSize
	) {}
}

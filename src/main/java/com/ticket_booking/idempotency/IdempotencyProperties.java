package com.ticket_booking.idempotency;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.idempotency")
public record IdempotencyProperties(
		long expirationMin
) {}

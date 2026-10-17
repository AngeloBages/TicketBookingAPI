package com.ticket_booking.booking;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties (
		@DefaultValue("15") long expirationMin,
		Expiration expiration,
		Cancellation cancellation
) {
	public record Expiration(
			int batchSize
	) {}
	
	public record Cancellation(
			int batchSize
	) {}
}

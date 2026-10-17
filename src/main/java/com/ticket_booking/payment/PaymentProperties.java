package com.ticket_booking.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
	    String currency,
	    String providerName
) {}
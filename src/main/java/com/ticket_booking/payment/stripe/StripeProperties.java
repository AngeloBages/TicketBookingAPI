package com.ticket_booking.payment.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.stripe")
public record StripeProperties (
            String secretKey,
            String webhookSecret,
            String successUrl,
            String cancelUrl
) {}


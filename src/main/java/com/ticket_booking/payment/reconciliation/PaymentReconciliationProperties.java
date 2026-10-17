package com.ticket_booking.payment.reconciliation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.reconciliation")
public record PaymentReconciliationProperties(
        Duration staleAfter,
		Duration claimTimeout,
        int batchSize
) {}
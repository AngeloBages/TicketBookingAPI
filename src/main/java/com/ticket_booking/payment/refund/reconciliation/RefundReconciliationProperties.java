package com.ticket_booking.payment.refund.reconciliation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.refund.reconciliation")
public record RefundReconciliationProperties(
		Duration staleAfter,
		Duration claimTimeout,
        int batchSize
) {}

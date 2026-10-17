package com.ticket_booking.payment.refund.recovery;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.refund.pending-recovery")
public record PendingRefundRecoveryProperties(
        Duration staleAfter,
        Duration claimTimeout,
        int batchSize
) {}

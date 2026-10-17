package com.ticket_booking.payment.recovery;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.payment.pending-recovery")
public record PendingPaymentRecoveryProperties(
        Duration staleAfter,
        Duration claimTimeout,
        int batchSize
) {}

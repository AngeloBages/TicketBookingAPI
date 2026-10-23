package com.ticket_booking.payment.recovery;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.observation.annotation.Observed;

@Component
public class PendingPaymentRecoveryJob {

    private final PendingPaymentRecoveryWorker worker;
    private final PendingPaymentRecoveryProperties properties;

    public PendingPaymentRecoveryJob(
            PendingPaymentRecoveryWorker worker,
            PendingPaymentRecoveryProperties properties) {
        this.worker = worker;
        this.properties = properties;
    }

    @Observed(
        name = "job.pending.payment.recovery",
        contextualName = "pending-payment-recovery-job"
    )
    @Scheduled(
        fixedDelayString =
            "${app.payment.pending-recovery.fixed-delay:30000}"
    )
    public void recoverPendingPayments() {

        worker.recover(
                properties.batchSize()
        );
    }
}
package com.ticket_booking.payment.refund.recovery;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.observation.annotation.Observed;

@Component
public class PendingRefundRecoveryJob {

    private final PendingRefundRecoveryWorker worker;
    private final PendingRefundRecoveryProperties properties;

    public PendingRefundRecoveryJob(
            PendingRefundRecoveryWorker worker,
            PendingRefundRecoveryProperties properties) {
        this.worker = worker;
        this.properties = properties;
    }

    @Observed(
        name = "job.pending.refund.recovery",
        contextualName = "pending-refund-recovery-job"
    )
    @Scheduled(
        fixedDelayString = 
        	"${app.payment.refund.pending-recovery.fixed-delay:30000}"
    )
    public void recoverPendingRefunds() {

        worker.recover(
                properties.batchSize()
        );
    }
}

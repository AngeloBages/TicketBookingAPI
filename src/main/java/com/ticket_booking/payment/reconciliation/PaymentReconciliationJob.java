package com.ticket_booking.payment.reconciliation;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.observation.annotation.Observed;

@Component
public class PaymentReconciliationJob {

    private final PaymentReconciliationWorker worker;
    private final PaymentReconciliationProperties properties;

    public PaymentReconciliationJob(
            PaymentReconciliationWorker worker,
            PaymentReconciliationProperties properties) {

        this.worker = worker;
        this.properties = properties;
    }

    @Observed(
        name = "job.payment.reconciliation",
        contextualName = "payment-reconciliation-job"
    )
    @Scheduled(
    	fixedDelayString = "${app.payment.reconciliation.fixed-delay:60000}"
    )
    public void reconcilePayments() {
    	Instant now = Instant.now();

    	worker.reconcile(
                now.minus(properties.staleAfter()),
                now.minus(properties.claimTimeout()),
                properties.batchSize()
        );
    }
}

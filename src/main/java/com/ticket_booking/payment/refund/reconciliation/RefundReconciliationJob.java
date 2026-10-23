package com.ticket_booking.payment.refund.reconciliation;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.observation.annotation.Observed;

@Component
public class RefundReconciliationJob {
	
	private final RefundReconciliationWorker worker;
	private final RefundReconciliationProperties properties;

	public RefundReconciliationJob(
			RefundReconciliationWorker worker,
			RefundReconciliationProperties properties) {

		this.worker = worker;
		this.properties = properties;
	}

	@Observed(
        name = "job.refund.reconciliation",
        contextualName = "refund-reconciliation-job"
    )
	@Scheduled(
		fixedDelayString = "${app.payment.refund.reconciliation.fixed-delay:300000}"
	)
	public void reconcileRefunds() {
		worker.reconcile(
				properties.batchSize()
		);
	}
}

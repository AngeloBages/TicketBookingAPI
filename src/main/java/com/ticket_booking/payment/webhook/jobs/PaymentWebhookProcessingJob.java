package com.ticket_booking.payment.webhook.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticket_booking.payment.webhook.PaymentWebhookWorker;
import com.ticket_booking.payment.webhook.PaymentWebhookProperties;

import io.micrometer.observation.annotation.Observed;

@Component
public class PaymentWebhookProcessingJob {

    private final PaymentWebhookWorker worker;
    private final PaymentWebhookProperties properties;

    public PaymentWebhookProcessingJob(
    		PaymentWebhookWorker worker,
    		PaymentWebhookProperties properties) {
        this.worker = worker;
        this.properties = properties;
    }

    @Observed(
        name = "job.payment.webhook.processing",
        contextualName = "payment-webhook-processing-job"
    )
    @Scheduled(
    	fixedDelayString = "${app.payment.webhook.processing.fixed-delay:1000}"
    )
    public void processWebhooks() {
    	worker.processPendingWebhooks(
    			properties.processing().batchSize()
    	);
    }
}

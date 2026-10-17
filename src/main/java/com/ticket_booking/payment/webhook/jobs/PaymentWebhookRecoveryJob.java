package com.ticket_booking.payment.webhook.jobs;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.ticket_booking.payment.webhook.PaymentWebhookProperties;
import com.ticket_booking.payment.webhook.repositories.IPaymentWebhookRepository;

import io.micrometer.observation.annotation.Observed;

public class PaymentWebhookRecoveryJob {
	
    private static final Logger log =
            LoggerFactory.getLogger(PaymentWebhookRecoveryJob.class);

	private final IPaymentWebhookRepository paymentWebhookRepository;
	private final PaymentWebhookProperties properties;

    public PaymentWebhookRecoveryJob(
    		IPaymentWebhookRepository paymentWebhookRepository,
    		PaymentWebhookProperties properties) {

        this.paymentWebhookRepository = paymentWebhookRepository;
        this.properties = properties;
    }

    @Observed(
        name = "job.payment.webhook.recovery",
        contextualName = "payment-webhook-recovery-job"
    )
    @Scheduled(
    	fixedDelayString = "${app.payment.webhook-recovery.fixed-delay:900000}"
    )
    public void processWebhooks() {    	
    	Instant staleBefore = 
    			Instant.now().minus(properties.staleAfter());
    	
    	int recovered = paymentWebhookRepository
    			.requeueStaleProcessingWebhooks(staleBefore);
    	
    	if (recovered > 0) {
            log.warn(
                    "Stale payment webhook requeued recoveredCount={}",
                    recovered
            );
        } else {
            log.debug("Payment webhook recovery completed recoveredCount=0");
        }
    }
}

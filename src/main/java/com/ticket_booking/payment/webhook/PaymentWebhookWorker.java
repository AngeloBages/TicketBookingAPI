package com.ticket_booking.payment.webhook;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;

@Component
public class PaymentWebhookWorker {
	
	private static final Logger log =
    		LoggerFactory.getLogger(PaymentWebhookWorker.class);

    private final PaymentWebhookProcessor processor;
    private final PaymentWebhookClaimer claimHandler;

    public PaymentWebhookWorker(
            PaymentWebhookProcessor processor,
            PaymentWebhookClaimer claimHandler) {
        this.processor = processor;
        this.claimHandler = claimHandler;
    }

	public void processPendingWebhooks(int limit) {

        List<Long> webhookIds = 
        		claimHandler.claimPendingWebhooks(limit);
        
        if (webhookIds.isEmpty()) {
            log.debug("Payment webhook processing found no pending webhooks");
            return;
        }
        
        log.info(
                "Payment webhook processing batch started claimedCount={}",
                webhookIds.size()
        );
        
        int processed = 0;
        int failed = 0;

        for (Long webhookId : webhookIds) {
        	try {
                if(processor.process(webhookId)) {
                	processed++;
                	
                } else {
                    failed++;

                    log.warn(
                            "Payment webhook marked failed webhookId={}",
                            webhookId
                    );
                }
                
                log.info(
                    "Stripe webhook processed webhookId={}",
                    webhookId
                );
                
        	} catch (IntegrationException ex) {
                failed++;
                
                IntegrationExceptionUtils.logFailure(
            			log, 
            			"Payment webhook integration attempt failed "
            					+ "webhookId={} failureType={} outcome={} errorCode={} "
            					+ "providerStatus={} errorMessage={}",
            			ex,
            			webhookId,
                        ex.getFailureType(),
                        ex.getOutcome(),
                        ex.getErrorCode(),
                        ex.getProviderStatusCode(),
                        ex.getMessage()
            	);

            } catch (RuntimeException ex) {
                failed++;

                log.error(
                        "Unexpected payment webhook processing failure webhookId={}",
                        webhookId,
                        ex
                );
            }
        }

        log.info(
                "Payment webhook processing batch completed "
                		+ "claimedCount={} processedCount={} failedCount={}",
                webhookIds.size(),
                processed,
                failed
        );
    }
}

package com.ticket_booking.payment.reconciliation;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;

@Component
public class PaymentReconciliationWorker {

	private static final Logger log = 
			LoggerFactory.getLogger(PaymentReconciliationWorker.class);

    private final PaymentReconciliationClaimer claimer;
    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationWorker(
            PaymentReconciliationClaimer claimer,
            PaymentReconciliationService reconciliationService) {
        this.claimer = claimer;
        this.reconciliationService = reconciliationService;
    }
	
	public void reconcile(
            Instant staleBefore,
            Instant claimExpiredBefore,
            int limit
    ) {	
        List<Long> paymentIds =
                claimer.claim(
                		staleBefore,
                        claimExpiredBefore,
                        limit
                );
        
        if (paymentIds.isEmpty()) {
            log.debug("Payment reconciliation found no candidates");
            return;
        }
        
        log.info(
                "Payment reconciliation batch started claimedCount={}",
                paymentIds.size()
        );
        
        int succeeded = 0;
        int failed = 0;

        for (Long paymentId : paymentIds) {
            try {
                reconciliationService.reconcile(paymentId);
                succeeded++;
                
            } catch (IntegrationException ex) {
            	failed++;
            	
            	IntegrationExceptionUtils.logFailure(
            			log, 
            			"Payment reconciliation provider call failed " 
            					+ "paymentId={} failureType={} outcome={} errorCode={} "
            					+ "providerStatus={} errorMessage={}",
            			ex,
            			paymentId,
                        ex.getFailureType(),
                        ex.getOutcome(),
                        ex.getErrorCode(),
                        ex.getProviderStatusCode(),
                        ex.getMessage()
            	);
            	
            } catch (RuntimeException ex) {
                failed++;

                log.error(
                        "Unexpected payment reconciliation failure paymentId={}",
                        paymentId,
                        ex
                );
            }
        }
        
        log.info(
                "Payment reconciliation batch completed "
                		+ "claimedCount={} completedCount={} failedCount={}",
                paymentIds.size(),
                succeeded,
                failed
        );
    }
}

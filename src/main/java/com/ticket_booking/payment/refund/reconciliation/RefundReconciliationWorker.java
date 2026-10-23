package com.ticket_booking.payment.refund.reconciliation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;

@Component
public class RefundReconciliationWorker {
	
	private static final Logger log = 
			LoggerFactory.getLogger(RefundReconciliationWorker.class);

    private final RefundReconciliationClaimer claimer;
    private final RefundReconciliationService reconciliationService;

    public RefundReconciliationWorker(
    		RefundReconciliationClaimer claimer,
    		RefundReconciliationService reconciliationService) {
        this.claimer = claimer;
        this.reconciliationService = reconciliationService;
    }
	
	public void reconcile(int limit) {
    	
        List<Long> refundIds =
                claimer.claim(limit);
        
        if (refundIds.isEmpty()) {
            log.debug("Refund reconciliation found no candidates");
            return;
        }
        
        log.info(
                "Refund reconciliation batch started claimedCount={}",
                 refundIds.size()
        );
        
        int succeeded = 0;
        int failed = 0;

        for (Long refundId : refundIds) {
            try {
                reconciliationService.reconcile(refundId);
                succeeded++;
                
            } catch (IntegrationException ex) {
            	failed++;
            	
            	IntegrationExceptionUtils.logFailure(
            			log, 
            			"Refund reconciliation provider call failed "
            					+ "refundId={} failureType={} outcome={} errorCode={} "
            					+ "providerStatus={} errorMessage={}",
            			ex,
            			refundId,
                        ex.getFailureType(),
                        ex.getOutcome(),
                        ex.getErrorCode(),
                        ex.getProviderStatusCode(),
                        ex.getMessage()
            	);
            	
            } catch (RuntimeException ex) {
                failed++;

                log.error(
                        "Unexpected refund reconciliation failure refundId={}",
                        refundId,
                        ex
                );
            }
            
            log.info(
                    "Refund reconciliation batch completed "
                    		+ "claimedCount={} completedCount={} failedCount={}",
                    refundIds.size(),
                    succeeded,
                    failed
            );
        }
    }
}

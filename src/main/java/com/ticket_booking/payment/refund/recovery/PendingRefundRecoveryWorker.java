package com.ticket_booking.payment.refund.recovery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;


@Component
public class PendingRefundRecoveryWorker {

	private static final Logger log =
            LoggerFactory.getLogger(
                    PendingRefundRecoveryWorker.class
            );

    private final PendingRefundClaimer claimer;
    private final PendingRefundRecoveryService recoveryService;

    public PendingRefundRecoveryWorker(
            PendingRefundClaimer claimer,
            PendingRefundRecoveryService recoveryService
    ) {
        this.claimer = claimer;
        this.recoveryService = recoveryService;
    }

    public void recover(int limit) {
    	
        List<Long> refundIds =
                claimer.claimStalePendingRefunds(limit);
        
        if (refundIds.isEmpty()) {
            log.debug("Pending refund recovery found no candidates");
            return;
        }
        
        log.info(
                "Pending refund recovery batch started claimedCount={}",
                refundIds.size()
        );
        
        int succeeded = 0;
        int failed = 0;

        for (Long refundId : refundIds) {
            try {
                recoveryService.recover(refundId);succeeded++;
                
            } catch (IntegrationException ex) {
            	failed++;
            	
            	IntegrationExceptionUtils.logFailure(
            			log, 
            			"Pending refund recovery provider call failed "
                                + "refundId={} failureType={} outcome={} errorCode={}"
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
                        "Unexpected pending refund recovery failure refundId={}",
                        refundId,
                        ex
                );
            }
        }
        
        log.info(
                "Pending refund recovery batch completed "
                		+ "claimedCount={} completedCount={} failedCount={}",
                refundIds.size(),
                succeeded,
                failed
        );
    }
}

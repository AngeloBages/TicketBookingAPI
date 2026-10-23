package com.ticket_booking.payment.recovery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;

@Component
public class PendingPaymentRecoveryWorker {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PendingPaymentRecoveryWorker.class
            );

    private final PendingPaymentClaimer claimer;
    private final PendingPaymentRecoveryService recoveryService;

    public PendingPaymentRecoveryWorker(
            PendingPaymentClaimer claimer,
            PendingPaymentRecoveryService recoveryService
    ) {
        this.claimer = claimer;
        this.recoveryService = recoveryService;
    }

    public void recover(int limit) {
        List<Long> paymentIds =
                claimer.claimStalePendingPayments(limit);
        
        if (paymentIds.isEmpty()) {
            log.debug("Pending payment recovery found no candidates");
            return;
        }
        
        log.info(
                "Pending payment recovery batch started claimedCount={}",
                paymentIds.size()
        );
        
        int succeeded = 0;
        int failed = 0;

        for (Long paymentId : paymentIds) {
            try {
                recoveryService.recover(paymentId);
                succeeded++;
                
            } catch (IntegrationException ex) {
            	failed++;
            	
            	IntegrationExceptionUtils.logFailure(
            			log, 
            			"Pending payment recovery provider call failed "
                                + "paymentId={} failureType={} outcome={} errorCode={}"
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
                        "Unexpected pending payment recovery failure paymentId={}",
                        paymentId,
                        ex
                );
            }
        }
        
        log.info(
                "Pending payment recovery batch completed "
                		+ "claimedCount={} completedCount={} failedCount={}",
                paymentIds.size(),
                succeeded,
                failed
        );
    }
}

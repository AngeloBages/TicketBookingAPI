package com.ticket_booking.payment.recovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.exceptions.PaymentReconciliationAlreadyClaimedException;
import com.ticket_booking.payment.repositories.IPaymentRepository;

@Component
public class PendingPaymentClaimer {

    private final IPaymentRepository paymentRepository;
    private final PendingPaymentRecoveryProperties properties;

    public PendingPaymentClaimer(
            IPaymentRepository paymentRepository,
            PendingPaymentRecoveryProperties properties
    ) {
        this.paymentRepository = paymentRepository;
        this.properties = properties;
    }

    @Transactional
    public List<Long> claimStalePendingPayments(int limit) {
        Instant now = Instant.now();
        
        Instant staleBefore = now.minus(properties.staleAfter());
        Instant claimExpiredBefore = now.minus(properties.claimTimeout());

        List<Payment> payments =
                paymentRepository.findStalePendingPaymentsForUpdate(
                        staleBefore,
                        claimExpiredBefore,
                        limit
                );

        List<Long> paymentIds = new ArrayList<>();

        for (Payment payment : payments) {
            payment.setReconciliationClaimedAt(now);
            paymentIds.add(payment.getId());
        }

        return paymentIds;
    }
    
    @Transactional
    public Payment claimPendingPayment(Long paymentId) {
        Instant now = Instant.now();
        
        Payment payment =
                paymentRepository
	                .findPendingPaymentForUpdate(paymentId)
	                .orElseThrow(PaymentNotFoundException::new);
        
        boolean isClaimed = 
        		payment.isReconciliationTimestampClaimed(
        				now, 
        				properties.claimTimeout()
        		);
        
        if(isClaimed) {
        	throw new PaymentReconciliationAlreadyClaimedException();
        }
        
        payment.setReconciliationClaimedAt(now);

        return payment;
    }
}

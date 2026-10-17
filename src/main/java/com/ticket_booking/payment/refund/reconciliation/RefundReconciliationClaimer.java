package com.ticket_booking.payment.refund.reconciliation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;

@Component
public class RefundReconciliationClaimer {

	private final IPaymentRefundRepository refundRepository;

    public RefundReconciliationClaimer(
    		IPaymentRefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    @Transactional
    public List<Long> claim(
            int limit,
            Instant claimExpiredBefore) {
    
        Instant now = Instant.now();

        List<PaymentRefund> payments = 
        		refundRepository.findRefundsForReconciliation(
		                Set.of(
		                        RefundStatus.PROCESSING.name(),
		                        RefundStatus.UNKNOWN.name()
		                ),
		                claimExpiredBefore,
		                limit
		        );

        List<Long> paymentIds = new ArrayList<>();

        for (PaymentRefund payment : payments) {
            payment.setReconciliationClaimedAt(now);
            paymentIds.add(payment.getId());
        }
        
        return paymentIds;
    }
}

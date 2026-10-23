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
	private final RefundReconciliationProperties properties;

    public RefundReconciliationClaimer(
    		IPaymentRefundRepository refundRepository,
    		RefundReconciliationProperties properties) {
        this.refundRepository = refundRepository;
        this.properties = properties;
    }

    @Transactional
    public List<Long> claim(int limit) {
    
        Instant now = Instant.now();

        Instant staleBefore = now.minus(properties.staleAfter());
        Instant claimExpiredBefore = now.minus(properties.claimTimeout());

        List<PaymentRefund> payments = 
        		refundRepository.findRefundsForReconciliation(
		                Set.of(
		                        RefundStatus.PROCESSING.name(),
		                        RefundStatus.UNKNOWN.name()
		                ),
		                staleBefore,
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

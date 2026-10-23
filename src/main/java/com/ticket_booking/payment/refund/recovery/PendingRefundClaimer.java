package com.ticket_booking.payment.refund.recovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;

@Component
public class PendingRefundClaimer {

	private final IPaymentRefundRepository refundRepository;
	private final PendingRefundRecoveryProperties properties;

    public PendingRefundClaimer(
            IPaymentRefundRepository refundRepository,
            PendingRefundRecoveryProperties properties
    ) {
        this.refundRepository = refundRepository;
        this.properties = properties;
    }

    @Transactional
    public List<Long> claimStalePendingRefunds(int limit) {
        Instant now = Instant.now();
        
        Instant staleBefore = now.minus(properties.staleAfter());
        Instant claimExpiredBefore = now.minus(properties.claimTimeout());

        List<PaymentRefund> refunds =
        		refundRepository.findStalePendingRefundsForUpdate(
                        staleBefore,
                        claimExpiredBefore,
                        limit
                );

        List<Long> refundIds = new ArrayList<>();

        for (PaymentRefund refund : refunds) {
        	refund.setReconciliationClaimedAt(now);
        	refundIds.add(refund.getId());
        }

        return refundIds;
    }
}

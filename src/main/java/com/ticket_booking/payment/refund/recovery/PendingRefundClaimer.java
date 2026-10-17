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

    public PendingRefundClaimer(
            IPaymentRefundRepository refundRepository
    ) {
        this.refundRepository = refundRepository;
    }

    @Transactional
    public List<Long> claimStalePendingRefunds(
            Instant staleBefore,
            Instant claimExpiredBefore,
            int limit
    ) {
        Instant now = Instant.now();

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

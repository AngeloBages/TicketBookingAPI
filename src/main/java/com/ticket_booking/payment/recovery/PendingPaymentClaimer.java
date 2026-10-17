package com.ticket_booking.payment.recovery;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.payment.repositories.IPaymentRepository;

@Service
public class PendingPaymentClaimer {

    private final IPaymentRepository paymentRepository;

    public PendingPaymentClaimer(
            IPaymentRepository paymentRepository
    ) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public List<Long> claimStalePendingPayments(
            Instant staleBefore,
            Instant claimExpiredBefore,
            int limit
    ) {
        Instant now = Instant.now();

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
}

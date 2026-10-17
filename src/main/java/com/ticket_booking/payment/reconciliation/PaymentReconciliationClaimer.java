package com.ticket_booking.payment.reconciliation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.payment.repositories.IPaymentRepository;

@Component
public class PaymentReconciliationClaimer {

    private final IPaymentRepository paymentRepository;

    public PaymentReconciliationClaimer(
            IPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public List<Long> claim(
            Instant staleBefore,
            Instant claimExpiredBefore,
            int limit
    ) {
        Instant now = Instant.now();

        List<Payment> payments = 
        		paymentRepository.findPaymentsForReconciliationForUpdate(
		                Set.of(
		                        PaymentStatus.PROCESSING.name(),
		                        PaymentStatus.UNKNOWN.name()
		                ),
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


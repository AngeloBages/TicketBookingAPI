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
    private final PaymentReconciliationProperties properties;

    public PaymentReconciliationClaimer(
            IPaymentRepository paymentRepository,
            PaymentReconciliationProperties properties) {
        this.paymentRepository = paymentRepository;
        this.properties = properties;
    }

    @Transactional
    public List<Long> claim(int limit) {
        Instant now = Instant.now();

        Instant staleBefore = now.minus(properties.staleAfter());
        Instant claimExpiredBefore = now.minus(properties.claimTimeout());
        
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


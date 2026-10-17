package com.ticket_booking.payment.recovery;

import org.springframework.stereotype.Service;

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.payment.PaymentOrchestrator;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.repositories.IPaymentRepository;

@Service
public class PendingPaymentRecoveryService {

    private final IPaymentRepository paymentRepository;
    private final PaymentOrchestrator paymentOrchestrator;

    public PendingPaymentRecoveryService(
            IPaymentRepository paymentRepository,
            PaymentOrchestrator paymentOrchestrator
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentOrchestrator = paymentOrchestrator;
    }

    public void recover(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException()
                );

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        if (payment.getProviderPaymentId() != null
                || payment.getProviderCheckoutSessionId() != null) {
            return;
        }

        paymentOrchestrator.recoverPendingPayment(payment);
    }
}
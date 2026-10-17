package com.ticket_booking.payment.refund.recovery;

import org.springframework.stereotype.Service;

import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.payment.PaymentOrchestrator;
import com.ticket_booking.payment.refund.exceptions.PaymentRefundNotFoundException;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;

@Service
public class PendingRefundRecoveryService {

	private final IPaymentRefundRepository refundRepository;
    private final PaymentOrchestrator paymentOrchestrator;

    public PendingRefundRecoveryService(
            IPaymentRefundRepository refundRepository,
            PaymentOrchestrator paymentOrchestrator
    ) {
        this.refundRepository = refundRepository;
        this.paymentOrchestrator = paymentOrchestrator;
    }

    public void recover(Long refundId) {
        PaymentRefund payment = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new PaymentRefundNotFoundException()
                );

        if (payment.getStatus() != RefundStatus.PENDING) {
            return;
        }

        if (payment.getProviderRefundId() != null) {
            return;
        }

        paymentOrchestrator.recoverPendingRefund(payment);
    }
}

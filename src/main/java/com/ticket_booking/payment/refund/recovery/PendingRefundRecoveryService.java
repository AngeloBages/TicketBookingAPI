package com.ticket_booking.payment.refund.recovery;

import org.springframework.stereotype.Service;

import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.payment.refund.PaymentRefundService;
import com.ticket_booking.payment.refund.exceptions.PaymentRefundNotFoundException;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;

@Service
public class PendingRefundRecoveryService {

	private final IPaymentRefundRepository refundRepository;
    private final PaymentRefundService refundService;

    public PendingRefundRecoveryService(
            IPaymentRefundRepository refundRepository,
            PaymentRefundService refundService
    ) {
        this.refundRepository = refundRepository;
        this.refundService = refundService;
    }

    public void recover(Long refundId) {
        PaymentRefund refund = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new PaymentRefundNotFoundException()
                );

        if (refund.getStatus() != RefundStatus.PENDING) {
            return;
        }

        if (refund.getProviderRefundId() != null) {
            return;
        }

        refundService.processRefund(refund.getId());
    }
}

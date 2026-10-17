package com.ticket_booking.payment.refund.reconciliation;

import org.springframework.stereotype.Service;

import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.payment.PaymentProviderResolver;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderRefundStatusCommand;
import com.ticket_booking.payment.exceptions.PaymentProviderException;
import com.ticket_booking.payment.refund.RefundStateService;
import com.ticket_booking.payment.refund.exceptions.PaymentRefundNotFoundException;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundStatusResult;

@Service
public class RefundReconciliationService {

	private final IPaymentRefundRepository refundRepository;
    private final RefundStateService refundStateService;
    private final PaymentProviderResolver paymentProviderResolver;

    public RefundReconciliationService(
    		IPaymentRefundRepository refundRepository,
            RefundStateService refundStateService,
            PaymentProviderResolver paymentProviderResolver) {

        this.refundRepository = refundRepository;
        this.refundStateService = refundStateService;
        this.paymentProviderResolver = paymentProviderResolver;
    }

    public void reconcile(Long paymentId) {
    	
    	PaymentRefund refund = refundRepository.findByIdWithPayment(paymentId)
                .orElseThrow(
                        () -> new PaymentRefundNotFoundException()
                );

        if (!isReconciliationCandidate(refund)) {
            return;
        }
    	
    	try {
    		RefundStatusResult result =
            		paymentProviderResolver.resolve(refund.getProvider())
            		.retrieveRefundStatus(
                    		 new RetrieveProviderRefundStatusCommand(
                                     refund.getUuid(),
                                     refund.getProviderRefundId(),
                                     refund.getPayment().getProviderPaymentId()
                             )
                    );

            refundStateService.reconcile(
            		refund.getId(),
            		result.status(),
            		result.failureCode(),
            		result.failureMessage()
            );

    	} catch (PaymentProviderException ex) {
    		if (ex.getOutcome() == OperationOutcome.UNKNOWN) {
		    		refundStateService
		    			.markOutcomeUnknown(paymentId);
		    }
    		throw ex;
    	}
    }

    private boolean isReconciliationCandidate(PaymentRefund refund) {
    	return switch (refund.getStatus()) {
    	
	        case PROCESSING,
	             UNKNOWN -> true;
	             
	        default -> false;
    	};
    }
}

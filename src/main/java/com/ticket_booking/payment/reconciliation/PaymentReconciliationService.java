package com.ticket_booking.payment.reconciliation;

import org.springframework.stereotype.Service;

import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.payment.PaymentProviderResolver;
import com.ticket_booking.payment.PaymentStateService;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderPaymentStatusCommand;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.exceptions.PaymentProviderException;
import com.ticket_booking.payment.repositories.IPaymentRepository;
import com.ticket_booking.payment.responses.PaymentProviderResponses.PaymentStatusResult;

@Service
public class PaymentReconciliationService {

    private final IPaymentRepository paymentRepository;
    private final PaymentStateService paymentStateService;
    private final PaymentProviderResolver paymentProviderResolver;

    public PaymentReconciliationService(
            IPaymentRepository paymentRepository,
            PaymentStateService paymentStateService,
            PaymentProviderResolver paymentProviderResolver) {

        this.paymentRepository = paymentRepository;
        this.paymentStateService = paymentStateService;
        this.paymentProviderResolver = paymentProviderResolver;
    }

    public void reconcile(Long paymentId) {
    	
    	Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(
                        () -> new PaymentNotFoundException()
                );

        if (!isReconciliationCandidate(payment)) {
            return;
        }
    	
    	try {
    		PaymentStatusResult result =
            		paymentProviderResolver.resolve(payment.getProvider())
            		.retrievePaymentStatus(
                    		 new RetrieveProviderPaymentStatusCommand(
                                     payment.getUuid(),
                                     payment.getProviderPaymentId(),
                                     payment.getProviderCheckoutSessionId()
                             )
                    );

            paymentStateService.reconcile(
            		payment.getId(),
            		result.status(),
            		result.failureCode(),
            		result.failureMessage()
            );

    	} catch (PaymentProviderException ex) {
    		if (ex.getOutcome() == OperationOutcome.UNKNOWN) {
		    		paymentStateService
		    			.markOutcomeUnknown(paymentId);
	    	}
    		throw ex;
    	}
    }

    private boolean isReconciliationCandidate(Payment payment) {
    	return switch (payment.getStatus()) {
    	
	        case PROCESSING,
	             UNKNOWN -> true;
	             
	        default -> false;
    	};
    }
}

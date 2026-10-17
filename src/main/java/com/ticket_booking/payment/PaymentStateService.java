package com.ticket_booking.payment;


import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.DomainEventDispatcher;
import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.repositories.IPaymentRepository;

@Component
public class PaymentStateService {
	
	private final IPaymentRepository paymentRepository;
	private final DomainEventDispatcher dispatcher;

    public PaymentStateService(
    		IPaymentRepository paymentRepository,
    		DomainEventDispatcher dispatcher) {
        this.paymentRepository = paymentRepository;
        this.dispatcher = dispatcher;
    }
    
    @Transactional
    public void reconcile(
    		Long paymentId,
            ProviderPaymentStatus status,
            String failureCode,
            String failureMessage) {

    	Payment payment = findPaymentForUpdate(paymentId);
    	
        switch (status) {

            case SUCCEEDED ->
	            succeed(payment);

            case FAILED ->
            	fail(
            		payment,
                    failureCode,
                    failureMessage
                );

            case CANCELED ->
            	cancel(payment);
            
            case REQUIRES_ACTION ->
            	markRequiresAction(payment);

            case PROCESSING -> {
                // Nothing to transition.
            }
        }
        
        if(status != ProviderPaymentStatus.PROCESSING) {
            payment.clearReconciliationTimestamp();
        }
    }
    
    @Transactional
    public void succeed(
    		UUID paymentUuid,
    		String providerPaymentId,
    		String checkoutSessionId) {
    	Payment payment = findPaymentForUpdate(paymentUuid);
    	
    	payment.attachProviderPaymentId(providerPaymentId);
    	
    	if(checkoutSessionId != null) {
    		payment.attachProviderCheckoutSessionId(checkoutSessionId);
    	}
    	
    	succeed(payment);
    }
    
    private void succeed(Payment payment) {
    	payment.capture();
    	dispatcher.publishEventsFor(payment);
    }
    
    @Transactional
    public void fail(
    		Long paymentId,
            String failureCode,
            String failureMessage) {
    	Payment payment = findPaymentForUpdate(paymentId);
    	fail(
    		payment,
            failureCode,
            failureMessage
        );
    }
    
    @Transactional
    public void fail(
    		UUID paymentUuid,
			String providerPaymentId,
    		String checkoutSessionId,
            String failureCode,
            String failureMessage) {
    	Payment payment = findPaymentForUpdate(paymentUuid);
    	
    	payment.attachProviderPaymentId(providerPaymentId);
    	
    	if(checkoutSessionId != null) {
    		payment.attachProviderCheckoutSessionId(checkoutSessionId);
    	}
    	
    	fail(
    		payment,
            failureCode,
            failureMessage
        );
    }
    
    private void fail(
    		Payment payment,
    		String failureCode,
            String failureMessage) {
    	
    	payment.markFailed(
                failureCode,
                failureMessage
        );
    	dispatcher.publishEventsFor(payment);
    }
    
    @Transactional
    public void cancel(
    		UUID paymentUuid,
    		String providerPaymentId,
    		String checkoutSessionId) {
    	Payment payment = findPaymentForUpdate(paymentUuid);
    	
    	payment.attachProviderPaymentId(providerPaymentId);
    	
    	if(checkoutSessionId != null) {
    		payment.attachProviderCheckoutSessionId(checkoutSessionId);
    	}
    	
    	cancel(payment);
    }
    
    private void cancel(Payment payment) {
    	payment.cancel();
    	dispatcher.publishEventsFor(payment);
    }
    
    @Transactional
    public void markRequiresAction(
    		UUID paymentUuid,
    		String providerPaymentId,
    		String checkoutSessionId) {
    	Payment payment = findPaymentForUpdate(paymentUuid);
    	
    	payment.attachProviderPaymentId(providerPaymentId);
    	
    	if(checkoutSessionId != null) {
    		payment.attachProviderCheckoutSessionId(checkoutSessionId);
    	}
    	
    	markRequiresAction(payment);
    }
    
    private void markRequiresAction(Payment payment) {
    	payment.markRequiresAction();
    }
    
    @Transactional
    public void markRefunded(Long paymentId) {
    	Payment payment = findPaymentForUpdate(paymentId);
    	payment.markRefunded();

    	dispatcher.publishEventsFor(payment);
    }
    
    @Transactional 
    public void markOutcomeUnknown(Long paymentId) {
    	Payment payment = findPaymentForUpdate(paymentId);
        payment.markUnknown();
        
    	dispatcher.publishEventsFor(payment);
    }

    private Payment findPaymentForUpdate(
			Long paymentId) {
		
		return paymentRepository
				.findByIdForUpdate(paymentId)
				.orElseThrow(
						() -> new PaymentNotFoundException()
				);
	}
    
    private Payment findPaymentForUpdate(
			UUID paymentUuid) {
		
		return paymentRepository
				.findByUuidForUpdate(paymentUuid)
				.orElseThrow(
						() -> new PaymentNotFoundException()
				);
	}
}

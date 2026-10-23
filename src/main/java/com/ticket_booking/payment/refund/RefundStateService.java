package com.ticket_booking.payment.refund;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.DomainEventDispatcher;
import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.payment.refund.exceptions.PaymentRefundNotFoundException;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundPaymentResult;

@Service
public class RefundStateService {
	
	private final IPaymentRefundRepository refundRepository;
	private final DomainEventDispatcher dispatcher;

	public RefundStateService(
			IPaymentRefundRepository refundRepository,
			DomainEventDispatcher dispatcher) {
		this.refundRepository = refundRepository;
		this.dispatcher = dispatcher;
	}
	
	@Transactional
	public RefundStatus handleResultStatus(
			Long refundId,
			RefundPaymentResult result) {

		PaymentRefund refund = findRefundForUpdate(refundId);

		refund.attachProviderRefundId(result.providerRefundId());
		
		switch (result.status()) {

		    case SUCCEEDED ->
		       succeed(
		            refund
		        );
	
		    case FAILED ->
		        fail(
		            refund,
		            result.failureCode(),
		            result.failureMessage()
		        );
			    
			case CANCELED ->
				cancel(
					refund
				);
				
			case REQUIRES_ACTION ->
				markRequiresAction(refund);
		       
		    case PROCESSING -> {
		    	//webhook/reconciliation completes it.
		    }
		}
		
		return refund.getStatus();
	}
	
	@Transactional
    public void reconcile(
    		Long refundId,
            ProviderRefundStatus status,
            String failureCode,
            String failureMessage) {

    	PaymentRefund refund = findRefundForUpdate(refundId);
    	
        switch (status) {

            case SUCCEEDED -> {
            		succeed(refund);
	        }

            case FAILED -> {
            		fail(
            			refund,
                    	failureCode,
                        failureMessage
                    );
            }

            case CANCELED -> {
            		cancel(refund);
            }
            
            case REQUIRES_ACTION ->
            		markRequiresAction(refund);

            case PROCESSING -> {
                // Nothing to transition.
            }
        }
        
        if(status != ProviderRefundStatus.PROCESSING) {
            refund.clearReconciliationTimestamp();
        }
    }
	
	@Transactional
	public PaymentRefund markProcessing(Long refundId) {
		PaymentRefund refund = findRefundForUpdate(refundId);
		refund.markProcessing();
		return refund;
	}
	
	@Transactional
	public void markRequiresAction(
			UUID refundUuid,
			String providerRefundId) {
		PaymentRefund refund = findRefundForUpdate(refundUuid);
		markRequiresAction(refund);
	}
	
	private void markRequiresAction(PaymentRefund refund) {
		refund.markRequiresAction();
	}
	
	@Transactional
	public void succeed(
			UUID refundUuid,
			String providerRefundId) {
		
		PaymentRefund refund = findRefundForUpdate(refundUuid);
		
		refund.attachProviderRefundId(providerRefundId);
		succeed(refund);
	}
	
	private void succeed(PaymentRefund refund) {
	    refund.succeed();
	    dispatcher.publishEventsFor(refund);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void fail(
    		Long refundId,
    		String failureCode,
            String failureMessage) {
		
		PaymentRefund refund = findRefundForUpdate(refundId);
		fail(
			refund,
			failureCode,
			failureMessage
		);
	}
	
	@Transactional
	public void fail(
    		UUID refundUuid,
			String providerRefundId,
    		String failureCode,
            String failureMessage) {
		
		PaymentRefund refund = findRefundForUpdate(refundUuid);

		refund.attachProviderRefundId(providerRefundId);
		fail(
			refund,
			failureCode,
			failureMessage
		);
	}
	
	private void fail(
			PaymentRefund refund,
			String failureCode,
            String failureMessage) {

		refund.fail(
			failureCode,
			failureMessage
		);
	    dispatcher.publishEventsFor(refund);
	}
	
	@Transactional
	public void cancel(
			UUID refundUuid,
			String providerRefundId) {
		PaymentRefund refund = findRefundForUpdate(refundUuid);
		cancel(refund);
	}
	
	private void cancel(
			PaymentRefund refund) {
		refund.cancel();
	    dispatcher.publishEventsFor(refund);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markOutcomeUnknown(Long refundId) {
		PaymentRefund refund = findRefundForUpdate(refundId);
		refund.markUnknown();
	    dispatcher.publishEventsFor(refund);
	}
	
	private PaymentRefund findRefundForUpdate(Long refundId) {
		return refundRepository.findByIdForUpdate(refundId)
				.orElseThrow(
						() -> new PaymentRefundNotFoundException()
				);
	}
	
	private PaymentRefund findRefundForUpdate(UUID refundUuid) {
		return refundRepository.findByUuidForUpdate(refundUuid)
				.orElseThrow(
						() -> new PaymentRefundNotFoundException()
				);
	}
}

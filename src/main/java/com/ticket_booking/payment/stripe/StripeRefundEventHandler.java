package com.ticket_booking.payment.stripe;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.ticket_booking.payment.refund.RefundStateService;
import com.ticket_booking.payment.stripe.enums.StripeEventType;
import com.ticket_booking.payment.stripe.utils.StripeUtils;

@Component
public class StripeRefundEventHandler {
	
	private final RefundStateService refundStateService;

    public StripeRefundEventHandler(
            RefundStateService refundStateService) {
        this.refundStateService = refundStateService;
    }

    public void handle(
    		StripeEventType eventType,
    		Event event) {

    	switch (eventType) {

	    	case REFUND_UPDATED ->
	    		handleRefundUpdated(event);
	
	    	case REFUND_FAILED ->
	    		handleRefundFailed(event);
	
	    	default ->
	    		throw new IllegalStateException(eventType.name());
    	}
    }

    private void handleRefundUpdated(Event event) {
    	Refund refund = StripeUtils.getRefund(event);
    	UUID refundUuid = StripeUtils.requireRefundUuid(refund);

    	switch (StripeUtils.mapRefundStatus(refund)) {
	        case SUCCEEDED ->
	            refundStateService.succeed(
	                refundUuid,
	                refund.getId()
	            );
	
	        case FAILED ->
	            refundStateService.fail(
	                refundUuid,
	                refund.getId(),
	                refund.getFailureReason(),
	                null
	            );
	
	        case CANCELED ->
	            refundStateService.cancel(
	                refundUuid,
	                refund.getId()
	            );
	            
	        case REQUIRES_ACTION ->
	        	refundStateService.markRequiresAction(
	        			refundUuid,
		                refund.getId()
	        	);
	
	        case PENDING -> {}
    	}
    }

    private void handleRefundFailed(Event event) {
    	Refund refund = StripeUtils.getRefund(event);
    	UUID refundUuid = StripeUtils.requireRefundUuid(refund);

    	String failureCode = refund.getFailureReason();

    	refundStateService.fail(
    			refundUuid, 
    			refund.getId(),
    			failureCode, 
    			null
    	);
    }
}

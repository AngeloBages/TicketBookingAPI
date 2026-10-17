package com.ticket_booking.payment.stripe;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.ticket_booking.payment.PaymentStateService;
import com.ticket_booking.payment.stripe.enums.StripeEventType;
import com.ticket_booking.payment.stripe.enums.StripePaymentIntentStatus;
import com.ticket_booking.payment.stripe.enums.StripeSessionPaymentStatus;
import com.ticket_booking.payment.stripe.utils.StripeUtils;

@Component
public class StripePaymentEventHandler {

	private final PaymentStateService paymentStateService;
    private final StripeRetrieveOperations stripeRetrieve;

    public StripePaymentEventHandler (
    		PaymentStateService paymentStateService,
    		StripeRetrieveOperations stripeRetrieve) {
        this.paymentStateService = paymentStateService;
        this.stripeRetrieve = stripeRetrieve;
    }
    
    public void handle(
    		StripeEventType eventType,
    		Event event) {

    	switch (eventType) {
	
	    	case CHECKOUT_SESSION_COMPLETED ->
	        	handleCheckoutSessionCompleted(event);
			
			case CHECKOUT_SESSION_EXPIRED ->
			    handleCheckoutSessionExpired(event);
			
			case CHECKOUT_SESSION_ASYNC_PAYMENT_SUCCEEDED ->
			    handleCheckoutSessionAsyncPaymentSucceeded(event);
			
			case CHECKOUT_SESSION_ASYNC_PAYMENT_FAILED ->
			    handleCheckoutSessionAsyncPaymentFailed(event);
			
			case PAYMENT_INTENT_SUCCEEDED ->
			    handlePaymentIntentPaymentSucceeded(event);
			
			case PAYMENT_INTENT_PAYMENT_FAILED ->
			    handlePaymentIntentPaymentFailed(event);
			    
			case PAYMENT_INTENT_CANCELED ->
				handlePaymentIntentPaymentCanceled(event);
				
			case PAYMENT_INTENT_REQUIRES_ACTION ->
				handlePaymentIntentRequiresAction(event);
		
	    	default ->
	    		throw new IllegalStateException(eventType.name());
    	}
    }
    
    private void handleCheckoutSessionCompleted(Event event) {
    	Session session = StripeUtils.getSession(event);
    	
    	StripeSessionPaymentStatus paymentStatus = 
    			StripeUtils.mapSessionPaymentStatus(session);
    	
    	if (paymentStatus != StripeSessionPaymentStatus.PAID) {
            return;
        }
    	
    	UUID paymentUuid = StripeUtils.requirePaymentUuid(session);
        String providerPaymentId = StripeUtils.requirePaymentIntent(session);
        
        paymentStateService.succeed(
				paymentUuid,
				providerPaymentId,
				session.getId()
		);
    }
    
    private void handleCheckoutSessionExpired(Event event) {
    	Session session = StripeUtils.getSession(event);
    	
        UUID paymentUuid = StripeUtils.requirePaymentUuid(session);

    	String providerPaymentId =
                session.getPaymentIntent();

        if (providerPaymentId == null) {
            return;
        }
        
        PaymentIntent paymentIntent =
        		stripeRetrieve.retrievePaymentIntent(providerPaymentId);
        
        StripePaymentIntentStatus status = 
        		StripeUtils.mapPaymentIntentStatus(paymentIntent);
        
        switch (status) {

	        case SUCCEEDED ->
	        		paymentStateService.succeed(
	                        paymentUuid,
	                        providerPaymentId,
	                        session.getId()
	                );
	
	        case CANCELED ->
	        		paymentStateService.cancel(
	                        paymentUuid,
	                        providerPaymentId,
	                        session.getId()
	                );
	        		
	        case REQUIRES_ACTION ->
		        	paymentStateService.markRequiresAction(
		        			paymentUuid, 
		        			providerPaymentId, 
	                        session.getId()
		        	);
	        		
	        case PROCESSING,
	           	 REQUIRES_CONFIRMATION,
	        	 REQUIRES_PAYMENT_METHOD -> {
	            // Do nothing.
	        }
	
	        case REQUIRES_CAPTURE ->
			        throw new IllegalStateException(
		                    "Stripe PaymentIntent unexpectedly requires capture: "
		                            + paymentIntent.getId()
		            );
	    }
    }
    
    private void handleCheckoutSessionAsyncPaymentSucceeded(Event event) {
    	Session session = StripeUtils.getSession(event);
    	
        UUID paymentUuid = StripeUtils.requirePaymentUuid(session);
        String providerPaymentId = StripeUtils.requirePaymentIntent(session);
        
        paymentStateService.succeed(
				paymentUuid,
				providerPaymentId,
				session.getId()
		);
    }
    
    private void handleCheckoutSessionAsyncPaymentFailed(Event event) {
    	Session session = StripeUtils.getSession(event);

    	UUID paymentUuid = StripeUtils.requirePaymentUuid(session);
        String providerPaymentId = StripeUtils.requirePaymentIntent(session);
        
        PaymentIntent paymentIntent =
        		stripeRetrieve.retrievePaymentIntent(providerPaymentId);
    	
        String failureCode = 
        		StripeUtils.getFailureCode(paymentIntent);
        
        String failureMessage = 
        		StripeUtils.getFailureMessage(paymentIntent);
        
        paymentStateService.fail(
				paymentUuid,
				providerPaymentId, 
				session.getId(),
				failureCode,
				failureMessage
		);
	}

    private void handlePaymentIntentPaymentSucceeded(Event event) {
    	PaymentIntent paymentIntent = StripeUtils.getPaymentIntent(event);
    	
    	UUID paymentUuid = StripeUtils.requirePaymentUuid(paymentIntent);
        String providerPaymentId = paymentIntent.getId();
        
        paymentStateService.succeed(
				paymentUuid,
				providerPaymentId,
				null
		);
    }

    private void handlePaymentIntentPaymentFailed(Event event) {
    	PaymentIntent paymentIntent = StripeUtils.getPaymentIntent(event);
    	
    	UUID paymentUuid = StripeUtils.requirePaymentUuid(paymentIntent);
        String providerPaymentId = paymentIntent.getId();

        String failureCode = 
        		StripeUtils.getFailureCode(paymentIntent);
        
        String failureMessage = 
        		StripeUtils.getFailureMessage(paymentIntent);
        
        paymentStateService.fail(
				paymentUuid,
				providerPaymentId, 
				null,
				failureCode,
				failureMessage
		);
    }
    
    private void handlePaymentIntentPaymentCanceled(Event event) {
    	PaymentIntent paymentIntent = StripeUtils.getPaymentIntent(event);
    	
    	UUID paymentUuid = StripeUtils.requirePaymentUuid(paymentIntent);
        String providerPaymentId = paymentIntent.getId();
        
        paymentStateService.cancel(
        		paymentUuid,
        		providerPaymentId, 
        		null
        );
    }
    
    private void handlePaymentIntentRequiresAction(Event event) {
    	PaymentIntent paymentIntent = StripeUtils.getPaymentIntent(event);
    	
    	UUID paymentUuid = StripeUtils.requirePaymentUuid(paymentIntent);
        String providerPaymentId = paymentIntent.getId();
        
        paymentStateService.markRequiresAction(
        		paymentUuid,
        		providerPaymentId,
        		null
        );
    }
}

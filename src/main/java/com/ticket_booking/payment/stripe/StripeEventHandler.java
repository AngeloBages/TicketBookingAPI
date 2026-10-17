package com.ticket_booking.payment.stripe;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonSyntaxException;
import com.stripe.model.Event;
import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.stripe.enums.StripeEventType;
import com.ticket_booking.payment.stripe.utils.StripeUtils;
import com.ticket_booking.payment.webhook.IWebhookHandler;

@Component
public class StripeEventHandler implements IWebhookHandler {

    private static final Logger log =
            LoggerFactory.getLogger(StripeEventHandler.class);

    private final StripePaymentEventHandler paymentEventHandler;
	private final StripeRefundEventHandler refundEventHandler;

    public StripeEventHandler(
    		StripePaymentEventHandler paymentEventHandler,
    		StripeRefundEventHandler refunEventdHandler) {
        this.paymentEventHandler = paymentEventHandler;
    	this.refundEventHandler = refunEventdHandler;
    }

    @Override
    public void handle(PaymentWebhook webhook) {

    	Optional<StripeEventType> type =
                StripeEventType.from(webhook.getType());

        if (type.isEmpty()) {
        	log.debug(
                    "Ignoring unsupported payment provider webhook webhookId={} " 
                    		+ "providerEventId={} type={}",
                    webhook.getId(),
                    webhook.getProviderEventId(),
                    webhook.getType()
            );
            return;
        }

    	Event stripeEvent = deserializeEvent(webhook);
    	StripeEventType eventType = type.get();
        
    	switch (eventType.operation()) {
			
	    	case PAYMENT ->
	    		paymentEventHandler.handle(eventType, stripeEvent);
	    		
	    	case REFUND ->
	    		refundEventHandler.handle(eventType, stripeEvent);
    	}
    }
    
    private Event deserializeEvent(PaymentWebhook webhook) {
    	try {
    		Event stripeEvent = Event.GSON.fromJson(
                    webhook.getPayload(),
                    Event.class
            );

            if (stripeEvent == null) {
                throw StripeUtils.invalidWebhook(
                        "Stripe webhook payload deserialized to null",
                        null
                );
            }

            return stripeEvent;
            
        } catch (JsonSyntaxException ex) {
            throw StripeUtils.invalidWebhook(
            		"Unable to deserialize Stripe webhook payload", 
            		ex
            );
        }
    }

	@Override
	public PaymentProviderType getProviderType() {
		return PaymentProviderType.STRIPE;
	}
}

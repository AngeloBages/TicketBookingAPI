package com.ticket_booking.payment.stripe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.stripe.exceptions.InvalidStripeWebhookException;
import com.ticket_booking.payment.webhook.PaymentWebhookIngestionService;
import com.ticket_booking.payment.webhook.commands.PaymentWebhookCommands.ReceiveWebhookCommand;

@Component
public class StripeWebhookHandler {
	
	private static final Logger log =
	        LoggerFactory.getLogger(StripeWebhookHandler.class);
	
    private final PaymentWebhookIngestionService webhookIngestionService;
    private final StripeProperties properties;

    public StripeWebhookHandler(
            PaymentWebhookIngestionService webhookIngestionService,
            StripeProperties properties) {

        this.webhookIngestionService = webhookIngestionService;
        this.properties = properties;
    }

    public void handle(String signature, String payload) {

        Event event = verifyWebhook(signature, payload);
        
        log.info(
            "Stripe webhook received eventId={} eventType={}",
            event.getId(),
            event.getType()
        );

        webhookIngestionService.receive(
        		new ReceiveWebhookCommand(
		                PaymentProviderType.STRIPE,
		                event.getId(),
		                event.getType(),
		                payload
	            )
        );
    }
    
    private Event verifyWebhook(String signature, String payload) {

        try {
            return Webhook.constructEvent(
                    payload,
                    signature,
                    properties.webhookSecret()
            );
        } catch (SignatureVerificationException ex) {
            throw new InvalidStripeWebhookException(
            		ex.getCode(),
            		IntegrationFailureType.PROVIDER_ERROR,
            		OperationOutcome.FAILED,
            		ex.getStatusCode(),
                    "Invalid Stripe webhook signature",
                    ex
            );
        } catch (JsonSyntaxException ex) {
            throw new InvalidStripeWebhookException(
            		IntegrationFailureType.PROVIDER_ERROR,
            		OperationOutcome.FAILED,
                    "Invalid Stripe webhook payload",
                    ex
            );
        }
    }
}

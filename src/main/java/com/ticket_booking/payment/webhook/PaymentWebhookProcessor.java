package com.ticket_booking.payment.webhook;

import org.springframework.stereotype.Component;

import com.ticket_booking.common.exceptions.IntegrationException;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.payment.webhook.exceptions.InvalidPaymentWebhookStateException;
import com.ticket_booking.payment.webhook.exceptions.PaymentWebhookNotFoundException;
import com.ticket_booking.payment.webhook.repositories.IPaymentWebhookRepository;

@Component
public class PaymentWebhookProcessor {

    private final WebhookHandlerResolver webhookHandlerResolver;
    private final PaymentWebhookStateService webhookStateService;
    private final IPaymentWebhookRepository repository;

    public PaymentWebhookProcessor(
    		WebhookHandlerResolver webhookHandlerResolver,
            PaymentWebhookStateService webhookStateService,
            IPaymentWebhookRepository repository) {

        this.webhookHandlerResolver = webhookHandlerResolver;
        this.webhookStateService = webhookStateService;
        this.repository = repository;
    }

    public boolean process(Long webhookId) {
    	PaymentWebhook webhook = repository.findById(webhookId)
    			.orElseThrow(() ->
                		new PaymentWebhookNotFoundException(webhookId)
                );
		
		if (!webhook.isflaggedForProcessing()) {
			throw new InvalidPaymentWebhookStateException(
		            "Payment webhook must be flagged for PROCESSING before it can be processed"
		    );
		}
    	
    	try {
    		IWebhookHandler webhookHandler = 
    				webhookHandlerResolver.resolve(webhook.getProvider());
    		
    		webhookHandler.handle(webhook);
	
	        webhookStateService.markProcessed(webhookId);
	        
	        return true;
        
    	} catch (IntegrationException ex) {
    		if(ex.getOutcome() == OperationOutcome.FAILED) {
    			webhookStateService.fail(webhookId, ex.getMessage());
    			return false;
            }

            throw ex;
    	}
    }
}

package com.ticket_booking.payment.webhook;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.payment.webhook.repositories.IPaymentWebhookRepository;

@Component
public class PaymentWebhookClaimer {
	
	private final IPaymentWebhookRepository repository;

    public PaymentWebhookClaimer(
            IPaymentWebhookRepository repository) {
        this.repository = repository;
    }

	@Transactional
    public List<Long> claimPendingWebhooks(int limit) {
    	
    	List<PaymentWebhook> webhooks = repository.findPendingWebhooks(limit);
        List<Long> ids = new ArrayList<>();

        for (PaymentWebhook webhook : webhooks) {
            webhook.flagForProcessing();
            ids.add(webhook.getId());
        }
        
        return ids;
    }
}

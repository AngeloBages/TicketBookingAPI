package com.ticket_booking.payment.webhook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.payment.webhook.exceptions.PaymentWebhookNotFoundException;
import com.ticket_booking.payment.webhook.repositories.IPaymentWebhookRepository;

@Service
public class PaymentWebhookStateService {

	private final IPaymentWebhookRepository repository;

    public PaymentWebhookStateService(
            IPaymentWebhookRepository repository) {
        this.repository = repository;
    }
    
    @Transactional
    public void fail(Long paymentWebhookId, String errorMessage) {
        PaymentWebhook webhook = findPaymentWebhook(paymentWebhookId);
        webhook.markFailed(errorMessage);
    }
    
    @Transactional
    public void markProcessed(Long paymentWebhookId) {
    	PaymentWebhook webhook = findPaymentWebhook(paymentWebhookId);
        webhook.markProcessed();
    }
    
    private PaymentWebhook findPaymentWebhook(Long paymentWebhookId) {
    	return repository.findById(paymentWebhookId)
        		.orElseThrow(() -> new PaymentWebhookNotFoundException());
    }
}

package com.ticket_booking.payment.webhook;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.payment.webhook.commands.PaymentWebhookCommands.ReceiveWebhookCommand;
import com.ticket_booking.payment.webhook.repositories.IPaymentWebhookRepository;

@Service
public class PaymentWebhookIngestionService {

    private final IPaymentWebhookRepository repository;

    public PaymentWebhookIngestionService(
            IPaymentWebhookRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void receive(ReceiveWebhookCommand command) {
    	Instant now = Instant.now();

        repository.insertIfAbsent(
                command.provider().name(),
                command.providerEventId(),
                command.type(),
                command.payload(),
                now,
                now
        );
    }
}

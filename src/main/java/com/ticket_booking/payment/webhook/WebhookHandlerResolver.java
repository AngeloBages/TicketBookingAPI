package com.ticket_booking.payment.webhook;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.exceptions.UnsupportedPaymentProviderException;

@Component
public class WebhookHandlerResolver {

	private final List<IWebhookHandler> handlers;
	
	public WebhookHandlerResolver(
			List<IWebhookHandler> handlers) {
		this.handlers = handlers;
	}

    public IWebhookHandler resolve(PaymentProviderType paymentProvider) {

        return handlers.stream()
                .filter(provider ->
                        provider.getProviderType() == paymentProvider)
                .findFirst()
                .orElseThrow(() ->
                        new UnsupportedPaymentProviderException(
                        		paymentProvider.name()
                        )
                );
    }
}

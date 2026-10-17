package com.ticket_booking.payment.webhook;

import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.domain.models.enums.PaymentProviderType;

public interface IWebhookHandler {

	PaymentProviderType getProviderType();

	public void handle(PaymentWebhook webhook);
}

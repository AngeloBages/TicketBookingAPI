package com.ticket_booking.payment.refund.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.domain.events.refund.RefundRequestedEvent;
import com.ticket_booking.domain.events.refund.RefundSucceededEvent;
import com.ticket_booking.payment.PaymentStateService;
import com.ticket_booking.payment.refund.PaymentRefundService;

@Component
public class RefundEventListener {

	private final PaymentRefundService refundService;
	private final PaymentStateService paymentStateService;
	
	public RefundEventListener(
			PaymentRefundService refundService,
			PaymentStateService paymentStateService) {
		this.refundService = refundService;
		this.paymentStateService = paymentStateService;
	}
	
	@Async
	@TransactionalEventListener
	public void on(RefundRequestedEvent event) {
		refundService.processRefund(event.refundId());
	}
	
	@EventListener
	public void on(RefundSucceededEvent event) {
		paymentStateService.markRefunded(event.paymentId());
	}
}

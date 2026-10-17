package com.ticket_booking.payment.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.domain.events.payment.PaymentCanceledEvent;
import com.ticket_booking.domain.events.payment.PaymentCapturedEvent;
import com.ticket_booking.domain.events.payment.PaymentFailedEvent;
import com.ticket_booking.domain.events.payment.PaymentOutcomeUnknownEvent;
import com.ticket_booking.domain.events.payment.PaymentRefundedEvent;

@Component
public class PaymentOperationLogListener {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentOperationLogListener.class);
    
    @TransactionalEventListener
    public void onPaymentCanceled(PaymentCanceledEvent event) {
        log.info(
                "Payment canceled paymentId={}",
                event.paymentUuid()
        );
    }

    @TransactionalEventListener
    public void onPaymentCaptured(PaymentCapturedEvent event) {
        log.info(
                "Payment captured paymentId={}",
                event.paymentUuid()
        );
    }
    
    @TransactionalEventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn(
                "Payment failed paymentId={} failureCode={} failureMessage={}",
                event.paymentUuid(),
                event.failureCode(),
                event.failureMessage()
        );
    }

    @TransactionalEventListener
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        log.info(
                "Payment refunded paymentId={}",
                event.paymentUuid()
        );
    }
    
    @TransactionalEventListener
    public void onPaymentOutcomeUnknown(PaymentOutcomeUnknownEvent event) {
        log.warn(
                "Payment outcome unknown paymentId={}",
                event.paymentUuid()
        );
    }
}
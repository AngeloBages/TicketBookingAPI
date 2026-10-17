package com.ticket_booking.payment.refund.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ticket_booking.domain.events.refund.RefundCanceledEvent;
import com.ticket_booking.domain.events.refund.RefundFailedEvent;
import com.ticket_booking.domain.events.refund.RefundOutcomeUnknownEvent;
import com.ticket_booking.domain.events.refund.RefundSucceededEvent;

@Component
public class RefundOperationLogListener {

    private static final Logger log =
            LoggerFactory.getLogger(RefundOperationLogListener.class);

    @TransactionalEventListener
    public void onRefundCanceled(RefundCanceledEvent event) {
        log.info(
                "Refund canceled refundId={}",
                event.refundUuid()
        );
    }
    
    @TransactionalEventListener
    public void onRefundSucceeded(RefundSucceededEvent event) {
        log.info(
                "Refund succeeded refundId={}",
                event.refundUuid()
        );
    }
    
    @TransactionalEventListener
    public void onRefundFailed(RefundFailedEvent event) {
        log.warn(
                "Refund failed refundId={} failureCode={}" 
                		+ "failureMessage={}",
                event.refundUuid(),
                event.failureCode(),
                event.failureMessage()
        );
    }
    
    @TransactionalEventListener
    public void onRefundOutcomeUnknown(RefundOutcomeUnknownEvent event) {
        log.warn(
                "Refund outcome unknown refundId={}",
                event.refundUuid()
        );
    }
}

package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentRecoveryRequiredException extends DomainException {

    private static final long serialVersionUID = 1L;

    public PaymentRecoveryRequiredException() {
        super(
                HttpStatus.CONFLICT,
                "Payment Recovery Required",
                "The payment's Checkout Session is not currently available. "
                        + "The payment state must be recovered before "
                        + "checkout can be resumed."
        );
    }
}

package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class PaymentReconciliationAlreadyClaimedException extends DomainException {

    private static final long serialVersionUID = 1L;

    public PaymentReconciliationAlreadyClaimedException() {
        super(
            HttpStatus.CONFLICT,
            "Payment Reconciliation Already Claimed",
            "Payment reconciliation is already being processed."
        );
    }
}

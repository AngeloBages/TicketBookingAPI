package com.ticket_booking.payment.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class UnsupportedPaymentProviderException extends DomainException {

    private static final long serialVersionUID = 1L;

    public UnsupportedPaymentProviderException(String providerName) {
        super(
            HttpStatus.NOT_FOUND,
            "Unsupported Payment Provider",
            String.format("Payment provider '%s' is not supported or configured.", providerName)
        );
    }
}

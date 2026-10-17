package com.ticket_booking.domain.models.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.ticket_booking.payment.exceptions.InvalidPaymentFieldException;

public record PaymentAmount(BigDecimal value) {

	private static final int SCALE = 2;
    private static final BigDecimal MAX =
            new BigDecimal("99999999.99");

    public PaymentAmount {

        if (value == null) {
            throw new InvalidPaymentFieldException(
                    "amount",
                    "must not be null"
            );
        }

        if (value.signum() < 0) {
            throw new InvalidPaymentFieldException(
                    "amount",
                    "must not be negative"
            );
        }

        if (value.scale() > SCALE) {
            throw new InvalidPaymentFieldException(
                    "v",
                    "must have at most 2 decimal places"
            );
        }

        if (value.compareTo(MAX) > 0) {
            throw new InvalidPaymentFieldException(
                    "amount",
                    "must not exceed " + MAX
            );
        }

        value = value.setScale(
                SCALE,
                RoundingMode.UNNECESSARY
        );
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


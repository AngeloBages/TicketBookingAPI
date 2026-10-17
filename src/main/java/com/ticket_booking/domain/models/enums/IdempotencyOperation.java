package com.ticket_booking.domain.models.enums;

public enum IdempotencyOperation {

    PAYMENT_CREATE,
    BOOKING_CREATE,
    REFUND_CREATE
}

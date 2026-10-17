package com.ticket_booking.common.exceptions.enums;

public enum IntegrationFailureType {

    INVALID_REQUEST,
    AUTHENTICATION,
    AUTHORIZATION,
    NOT_FOUND,
    RATE_LIMITED,
    PROVIDER_ERROR,
    TRANSPORT_ERROR,
    TIMEOUT,
    PAYMENT_FAILED,
    UNKNOWN
}

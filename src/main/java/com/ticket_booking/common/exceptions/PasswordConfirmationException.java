package com.ticket_booking.common.exceptions;


import org.springframework.http.HttpStatus;

public class PasswordConfirmationException extends DomainException {

    private static final long serialVersionUID = -8607457833763597707L;

    public PasswordConfirmationException() {
        super(
            HttpStatus.BAD_REQUEST,
            "Password Confirmation Failed",
            "Password confirmation doesn't match."
        );
    }
}

package com.ticket_booking.user.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class SamePasswordException extends DomainException {

    private static final long serialVersionUID = -3494749624312285271L;

    public SamePasswordException() {
        super(
            HttpStatus.BAD_REQUEST,
            "Invalid Password",
            "The new password must be different from the current password."
        );
    }
}

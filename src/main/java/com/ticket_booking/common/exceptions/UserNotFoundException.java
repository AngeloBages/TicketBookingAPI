package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends DomainException {

    private static final long serialVersionUID = 5356588274153884955L;

    public UserNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "User Not Found",
            "User was not found."
        );
    }
}

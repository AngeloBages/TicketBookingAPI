package com.ticket_booking.user.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidCurrentPasswordException extends DomainException {

    private static final long serialVersionUID = -8095291706842032460L;

    public InvalidCurrentPasswordException() {
        super(
            HttpStatus.UNAUTHORIZED,
            "Invalid Current Password",
            "Current password is incorrect."
        );
    }
}

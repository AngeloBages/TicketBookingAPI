package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class InvalidBookingSeatStateException extends DomainException {

	private static final long serialVersionUID = -2943602280895479826L;

	public InvalidBookingSeatStateException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Invalid Booking State",
            message
        );
    }
}

package com.ticket_booking.booking.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class DuplicateBookingSeatException extends DomainException {

	private static final long serialVersionUID = 346764875876330034L;

	public DuplicateBookingSeatException() {
        super(
            HttpStatus.CONFLICT,
            "Duplicate Seat",
            "Event seat already exists for this booking."
        );
    }
}

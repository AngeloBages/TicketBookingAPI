package com.ticket_booking.venue.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class VenueNotFoundException extends DomainException {

	private static final long serialVersionUID = -419661064654425486L;

	public VenueNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "Venue Not Found",
            "Venue was not found."
        );
    }
}

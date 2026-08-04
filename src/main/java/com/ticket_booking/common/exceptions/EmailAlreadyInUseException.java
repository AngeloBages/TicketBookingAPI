package com.ticket_booking.common.exceptions;

public class EmailAlreadyInUseException extends DomainException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2586876346076701276L;

	public EmailAlreadyInUseException() {
        super("Email is already in use.");
    }

    public EmailAlreadyInUseException(String email) {
        super("Email is already in use: " + email);
    }
}

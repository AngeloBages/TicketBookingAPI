package com.ticket_booking.common.exceptions;

public abstract class DomainException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected DomainException(String message) {
        super(message);
    }

}

package com.ticket_booking.common.exceptions;


public class PasswordConfirmationException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8607457833763597707L;

	public PasswordConfirmationException() {
		super("Password confirmation doesn't match");
	}
}

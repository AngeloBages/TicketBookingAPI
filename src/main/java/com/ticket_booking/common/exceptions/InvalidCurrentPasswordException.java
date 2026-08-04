package com.ticket_booking.common.exceptions;

public class InvalidCurrentPasswordException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8095291706842032460L;

	public InvalidCurrentPasswordException() {
		super("Current password is incorrect.");
	}
}

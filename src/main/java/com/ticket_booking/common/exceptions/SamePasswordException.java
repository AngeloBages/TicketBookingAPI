package com.ticket_booking.common.exceptions;

public class SamePasswordException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3494749624312285271L;

	public SamePasswordException() {
		super("The new password must be different from the current password.");
	}
}

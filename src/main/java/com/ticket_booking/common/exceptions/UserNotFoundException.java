package com.ticket_booking.common.exceptions;

public class UserNotFoundException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5356588274153884955L;
	
	public UserNotFoundException() {
		super("User was not found.");
	}

}

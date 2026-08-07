package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public class LastAdminUserException extends DomainException {

	private static final long serialVersionUID = 5569182640799010245L;

	public LastAdminUserException() {
		super(
			HttpStatus.BAD_REQUEST,
			"Last Administrator User",
			"Last Administrator can't have their ADMIN role revoked");
	}
}

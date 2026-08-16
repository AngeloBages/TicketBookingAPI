package com.ticket_booking.admin.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class LastAdminUserException extends DomainException {

	private static final long serialVersionUID = 5569182640799010245L;

	public LastAdminUserException(String reason) {
		super(
			HttpStatus.BAD_REQUEST,
			"Last Administrator User",
			reason);
	}
}

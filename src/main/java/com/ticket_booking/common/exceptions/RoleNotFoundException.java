package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends DomainException {

	private static final long serialVersionUID = 7225526609363971218L;

	public RoleNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "Role Not Found",
            "Role was not found."
        );
    }
	
	public RoleNotFoundException(String role) {
        super(
            HttpStatus.NOT_FOUND,
            "Role Not Found",
            "Role was not found: " + role
        );
    }
}

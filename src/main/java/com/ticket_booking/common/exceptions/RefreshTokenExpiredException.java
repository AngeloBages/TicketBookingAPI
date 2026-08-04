package com.ticket_booking.common.exceptions;

public class RefreshTokenExpiredException extends DomainException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1990597054003302109L;

	public RefreshTokenExpiredException() {
        super("Refresh token has expired.");
    }

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}

package com.ticket_booking.common.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8263489383113746946L;

	public RefreshTokenNotFoundException() {
        super("Refresh token not found.");
    }

    public RefreshTokenNotFoundException(String token) {
        super("Refresh token not found: " + token);
    }
}
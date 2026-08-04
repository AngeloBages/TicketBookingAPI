package com.ticket_booking.common.exceptions;

public class RefreshTokenReplayException extends DomainException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6693160110248458860L;

	public RefreshTokenReplayException() {
        super("Refresh token has already been used.");
    }

    public RefreshTokenReplayException(String message) {
        super(message);
    }
}

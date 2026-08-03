package com.ticket_booking.common.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8066228242668823840L;

	public InvalidRefreshTokenException() {
		super("Invalid refresh token.");
	}
}

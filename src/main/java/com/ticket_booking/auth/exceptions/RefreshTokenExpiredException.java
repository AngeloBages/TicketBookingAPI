package com.ticket_booking.auth.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class RefreshTokenExpiredException extends DomainException {

    private static final long serialVersionUID = 1990597054003302109L;

    public RefreshTokenExpiredException() {
        this("Refresh token has expired.");
    }

    public RefreshTokenExpiredException(String message) {
        super(
            HttpStatus.UNAUTHORIZED,
            "Refresh Token Expired",
            message
        );
    }
}

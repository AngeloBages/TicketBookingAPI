package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public class RefreshTokenReplayException extends DomainException {

    private static final long serialVersionUID = 6693160110248458860L;

    public RefreshTokenReplayException() {
        this("Refresh token has already been used.");
    }

    public RefreshTokenReplayException(String message) {
        super(
            HttpStatus.CONFLICT,
            "Refresh Token Replay Detected",
            message
        );
    }
}

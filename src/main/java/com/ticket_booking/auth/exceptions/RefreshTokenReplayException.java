package com.ticket_booking.auth.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class RefreshTokenReplayException extends DomainException {

    private static final long serialVersionUID = 6693160110248458860L;

    public RefreshTokenReplayException() {
        super(
            HttpStatus.CONFLICT,
            "Refresh Token Replay Detected",
            "Replay attack detected for refresh token."
        );
    }
}

package com.ticket_booking.auth.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class RefreshTokenNotFoundException extends DomainException {

    private static final long serialVersionUID = 8263489383113746946L;
    
    public RefreshTokenNotFoundException() {
        super(
            HttpStatus.NOT_FOUND,
            "Refresh Token Not Found",
            "Refresh token not found."
        );
    }

    public RefreshTokenNotFoundException(String token) {
        super(
            HttpStatus.NOT_FOUND,
            "Refresh Token Not Found",
            "Refresh token not found: " + token
        );
    }
}
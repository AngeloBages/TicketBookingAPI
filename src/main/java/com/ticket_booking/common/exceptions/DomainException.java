package com.ticket_booking.common.exceptions;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final HttpStatus status;
    private final String title;

    protected DomainException(
            HttpStatus status,
            String title,
            String message) {

        super(message);
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}

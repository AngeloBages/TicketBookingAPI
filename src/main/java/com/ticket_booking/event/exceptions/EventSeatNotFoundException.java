package com.ticket_booking.event.exceptions;

import org.springframework.http.HttpStatus;

import com.ticket_booking.common.exceptions.DomainException;

public class EventSeatNotFoundException extends DomainException{

	private static final long serialVersionUID = 2308124467576891339L;

	public EventSeatNotFoundException() {
		super(
	            HttpStatus.NOT_FOUND,
	            "Event Seat Not Found",
	            "Event seat was not found."
	        );
	}
}

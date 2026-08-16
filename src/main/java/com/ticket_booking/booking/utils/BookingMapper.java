	package com.ticket_booking.booking.utils;

import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.responses.BookingResponses.EventSummaryResponse;
import com.ticket_booking.booking.responses.BookingResponses.BookingSeatSummaryResponse;
import com.ticket_booking.booking.responses.BookingResponses.VenueSummaryResponse;
import com.ticket_booking.domain.models.Booking;

public final class BookingMapper {
	
	private BookingMapper() {}

	public static BookingResponse toResponse(Booking booking) {

	    return new BookingResponse(
	            booking.getUuid(),
	            booking.getStatus(),
	            booking.getBookedAt(),
	            booking.getTotalPrice(),
	            
	            new EventSummaryResponse(
	                    booking.getEvent().getUuid(),
	                    booking.getEvent().getTitle(),
	                    booking.getEvent().getDescription(),
	                    booking.getEvent().getDate(),
	                    booking.getEvent().getPrice()
	            ),

	            new VenueSummaryResponse(
	                    booking.getEvent().getVenue().getUuid(),
	                    booking.getEvent().getVenue().getName(),
	                    booking.getEvent().getVenue().getAddress()
	            ),

	            booking.getBookingSeats()
	                    .stream()
	                    .map(bookingSeat -> new BookingSeatSummaryResponse(
	                            bookingSeat.getEventSeat().getSeat().getNumber(),
	                            bookingSeat.getEventSeat().getSeat().getRow(),
	                            bookingSeat.getEventSeat().getStatus(),
	                            bookingSeat.getEventSeat().getPrice()
	                    ))
	                    .toList()
	    );
	}
}

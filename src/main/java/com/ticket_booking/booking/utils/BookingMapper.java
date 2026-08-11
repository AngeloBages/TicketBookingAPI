	package com.ticket_booking.booking.utils;

import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.responses.BookingResponses.EventSummaryResponse;
import com.ticket_booking.booking.responses.BookingResponses.SeatResponse;
import com.ticket_booking.booking.responses.BookingResponses.VenueSummaryResponse;
import com.ticket_booking.domain.models.Booking;

public class BookingMapper {

	public static BookingResponse toResponse(Booking booking) {

	    return new BookingResponse(
	            booking.getUuid(),
	            booking.getStatus(),
	            booking.getBookedAt(),
	            booking.getTotalPrice(),
	            
	            new EventSummaryResponse(
	                    booking.getEvent().getUuid(),
	                    booking.getEvent().getTitle(),
	                    booking.getEvent().getDate(),
	                    booking.getEvent().getPrice()
	            ),

	            new VenueSummaryResponse(
	                    booking.getEvent().getVenue().getUuid(),
	                    booking.getEvent().getVenue().getName(),
	                    booking.getEvent().getVenue().getAddress()
	            ),

	            booking.getSeats()
	                    .stream()
	                    .map(seat -> new SeatResponse(
	                            seat.getRow(),
	                            seat.getNumber()
	                    ))
	                    .toList()
	    );
	}
}

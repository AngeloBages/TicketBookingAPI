	package com.ticket_booking.booking.utils;

import java.util.List;

import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.responses.BookingResponses.EventSummaryResponse;
import com.ticket_booking.booking.responses.BookingResponses.BookingSeatSummaryResponse;
import com.ticket_booking.booking.responses.BookingResponses.VenueSummaryResponse;
import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.BookingSeat;

public final class BookingMapper {
	
	private BookingMapper() {}
	
	public static BookingResponse toResponse(Booking booking, List<BookingSeat> bookingSeats) {

	    return new BookingResponse(
	            booking.getUuid(),
	            booking.getStatus(),
	            booking.getBookedAt(),
	            booking.getTotalPrice(),
	            
	            new EventSummaryResponse(
	                    booking.getEvent().getUuid(),
	                    booking.getEvent().getTitle(),
	                    booking.getEvent().getDescription(),
	                    booking.getEvent().getStartsAtAtVenue(),
	                    booking.getEvent().getPrice()
	            ),

	            new VenueSummaryResponse(
	                    booking.getEvent().getVenue().getUuid(),
	                    booking.getEvent().getVenue().getName(),
	                    booking.getEvent().getVenue().getAddress(),
	                    booking.getEvent().getVenue().getTimeZone().getId()
	            ),

	            bookingSeats
	            	.stream()
	                .map(bookingSeat -> new BookingSeatSummaryResponse(
	                        bookingSeat.getEventSeat().getSeat().getNumber(),
	                        bookingSeat.getEventSeat().getSeat().getRow(),
	                        bookingSeat.getPrice()
	                ))
	                .toList()
	    );
	}
}

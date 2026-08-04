	package com.ticket_booking.booking;

import com.ticket_booking.booking.dtos.BookingDtos.BookingResponse;
import com.ticket_booking.booking.dtos.BookingDtos.EventSummaryResponse;
import com.ticket_booking.booking.dtos.BookingDtos.SeatResponse;
import com.ticket_booking.booking.dtos.BookingDtos.VenueSummaryResponse;
import com.ticket_booking.domain.models.Booking;

public class BookingMapper {

	public static BookingResponse toResponse(Booking booking) {

	    return new BookingResponse(
	            booking.getUuid(),
	            booking.getStatus(),
	            booking.getBookingDate(),
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

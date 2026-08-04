package com.ticket_booking.booking.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.domain.models.Booking;
import static com.ticket_booking.booking.dtos.BookingDtos.*;

@Service
public class BookingService {
	
	private final IBookingRepository bookingRepository;
	
	public BookingService(IBookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getUserBookings(Long userId) {

	    return bookingRepository.findAllBookingsByUserId(userId)
	    		.stream()
	            .map(booking -> toResponse(booking))
	            .toList();
	}
	
	private BookingResponse toResponse(Booking booking) {

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

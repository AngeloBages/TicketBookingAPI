package com.ticket_booking.booking;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.repositories.IBookingRepository;
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
	            .map(booking -> BookingMapper.toResponse(booking))
	            .toList();
	}
}

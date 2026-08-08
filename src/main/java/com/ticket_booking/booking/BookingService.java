package com.ticket_booking.booking;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.booking.responses.BookingResponses.BookingResponse;
import com.ticket_booking.booking.utils.BookingCursor;
import com.ticket_booking.booking.utils.BookingMapper;
import com.ticket_booking.booking.utils.BookingCursorDto;
import com.ticket_booking.common.CursorPage;
import com.ticket_booking.domain.models.Booking;

@Service
public class BookingService {
	
	private final IBookingRepository bookingRepository;
	
	public BookingService(IBookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	@Transactional(readOnly = true)
	public CursorPage<BookingResponse> getUserBookings(Long userId, String cursor, int limit) {

		Pageable pageable = PageRequest.of(0, limit + 1);

        List<Booking> bookings;

        if (Strings.isBlank(cursor)) {

            bookings = bookingRepository.findFirstPage(
                    userId,
                    pageable);

        } else {

            BookingCursorDto decoded = BookingCursor.decode(cursor);

            bookings = bookingRepository.findNextPage(
                    userId,
                    decoded.bookingDate(),
                    decoded.id(),
                    pageable);
        }

        boolean hasNext = bookings.size() > limit;

        if (hasNext) {
            bookings.remove(limit);
        }

        String nextCursor = null;

        if (hasNext && !bookings.isEmpty()) {
            nextCursor = BookingCursor.encode(
                    bookings.get(bookings.size() - 1));
        }
        
        List<Long> ids = bookings.stream()
                .map(booking -> booking.getId())
                .toList();
		
        Map<Long, Booking> bookingMap = bookingRepository.fetchBookings(ids)
                .stream()
                .collect(Collectors.toMap(
	                		booking -> booking.getId(), 
	                		booking -> booking)
                		);

        List<BookingResponse> responses = ids.stream()
                .map(id -> bookingMap.get(id))
                .map(booking -> BookingMapper.toResponse(booking))
                .toList();
		
		return new CursorPage<>(
				responses,
				nextCursor,
				hasNext
		);
	}
}

package com.ticket_booking.booking.utils;

import java.util.stream.Collectors;

import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.Event;
import com.ticket_booking.domain.models.Seat;
import com.ticket_booking.domain.models.Venue;

public class BookingPaymentMapper {
	
    public static String toProductName(Booking booking) {
        return booking.getEvent().getTitle();
    }

    public static String toCheckoutDescription(Booking booking) {
        Event event = booking.getEvent();
        Venue venue = event.getVenue();

        String seats = booking.getBookingSeats().stream()
                .map(bookingSeat -> {
                    Seat seat = bookingSeat.getEventSeat().getSeat();

                    return "Row " + seat.getRow()
                            + ", Seat " + seat.getNumber()
                            + " (" + bookingSeat.getPrice() + ")";
                })
                .collect(Collectors.joining(", "));

        return String.format(
                "%s at %s, %s. Seats: %s. Total: %s. Status: %s.",
                event.getTitle(),
                venue.getName(),
                venue.getAddress(),
                seats,
                booking.getTotalPrice(),
                booking.getStatus()
        );
    }
}

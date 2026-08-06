package com.ticket_booking.booking.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import com.ticket_booking.domain.models.Booking;

public final class BookingCursor {

    private BookingCursor() {
    }

    public static String encode(Booking booking) {

        String value = booking.getBookingDate()
                + "|" + booking.getId();

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static BookingCursorDto decode(String cursor) {

        String decoded = new String(
                Base64.getUrlDecoder().decode(cursor),
                StandardCharsets.UTF_8
        );

        String[] parts = decoded.split("\\|", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor.");
        }

        return new BookingCursorDto(
                LocalDateTime.parse(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}

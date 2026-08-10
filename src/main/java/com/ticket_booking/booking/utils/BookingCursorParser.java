package com.ticket_booking.booking.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import org.apache.logging.log4j.util.Strings;

import com.ticket_booking.booking.exceptions.InvalidBookingCursorException;
import com.ticket_booking.venue.exceptions.InvalidVenueCursorException;

public final class BookingCursorParser {
	
	private static final int MAX_CURSOR_LENGTH = 512;

    private BookingCursorParser() {
    }

    public static String encode(BookingCursor bookingCursor) {

        String value = bookingCursor.bookingDate()
                + "|" + bookingCursor.id();

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static BookingCursor decode(String cursor) {

    	if (Strings.isBlank(cursor)
                || cursor.length() > MAX_CURSOR_LENGTH) {
            throw new InvalidBookingCursorException();
        }
    	
    	try {
    		String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );

            String[] parts = decoded.split("\\|", -1);

            if (parts.length != 2 
            		|| parts[0].isBlank()
                    || parts[1].isBlank()) {
                throw new InvalidBookingCursorException();
            }

            return new BookingCursor(
                    LocalDateTime.parse(parts[0]),
                    Long.parseLong(parts[1])
            );
            
    	} catch (IllegalArgumentException ex) {
            throw new InvalidVenueCursorException();
        }
    }
}

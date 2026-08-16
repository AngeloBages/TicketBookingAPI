package com.ticket_booking.venue.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.util.Strings;

import com.ticket_booking.venue.exceptions.InvalidVenueCursorException;


public final class VenueCursorParser {
	
	private static final int MAX_CURSOR_LENGTH = 128;

    private VenueCursorParser() {
    }

    public static String encode(VenueCursor venueCursor) {

        String value = Long.toString(venueCursor.venueId());

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static VenueCursor decode(String cursor) {
    	
    	if (Strings.isBlank(cursor)
                || cursor.length() > MAX_CURSOR_LENGTH) {
            throw new InvalidVenueCursorException();
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );

            long id = Long.parseLong(decoded);

            if (id <= 0) {
                throw new InvalidVenueCursorException();
            }

            return new VenueCursor(id);

        } catch (IllegalArgumentException ex) {
            throw new InvalidVenueCursorException();
        }
    }
}

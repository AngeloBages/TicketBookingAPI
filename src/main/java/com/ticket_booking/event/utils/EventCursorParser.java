package com.ticket_booking.event.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.apache.logging.log4j.util.Strings;

import com.ticket_booking.event.exceptions.InvalidEventCursorException;

public final class EventCursorParser {

	private static final int MAX_CURSOR_LENGTH = 512;
	
	public static String encode(EventCursor cursor) {
		
		String encoded = cursor.eventDateTime() + "|" + cursor.eventId();
		
		return Base64.getUrlEncoder()
                .withoutPadding()
				.encodeToString(encoded.getBytes(StandardCharsets.UTF_8));
	}
	
	public static EventCursor decode(String cursor) {
		
		if(Strings.isBlank(cursor)
                || cursor.length() > MAX_CURSOR_LENGTH){
			throw new InvalidEventCursorException();
		}
		
		try {
			String decoded = new String(
					Base64.getUrlDecoder().decode(cursor),
	                StandardCharsets.UTF_8
	        );
			
			String[] parts = decoded.split("\\|", -1);
			
			if(parts.length != 2 
					|| parts[0] == null
					|| parts[1] == null) {
				
				throw new InvalidEventCursorException();
			}
			
			return new EventCursor(
					Instant.parse(parts[0]),
					Long.valueOf(parts[1])
			);
			
		} catch (IllegalArgumentException ex) {
			throw new InvalidEventCursorException();
		}
	}
}

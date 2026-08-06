package com.ticket_booking.booking.utils;

import java.time.LocalDateTime;

public record BookingCursorDto(
	    LocalDateTime bookingDate,
	    Long id
) {}

package com.ticket_booking.domain.events;

import java.util.List;

public record BookingExpiredEvent(
		List<Long> bookingSeatIds
) {}

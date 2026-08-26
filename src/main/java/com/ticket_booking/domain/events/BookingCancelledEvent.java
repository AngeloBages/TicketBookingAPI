package com.ticket_booking.domain.events;

import java.util.List;

public record BookingCancelledEvent(
		List<Long> bookingSeatIds
) {}

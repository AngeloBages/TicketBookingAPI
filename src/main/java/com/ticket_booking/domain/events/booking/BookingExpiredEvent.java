package com.ticket_booking.domain.events.booking;

import java.util.UUID;

public record BookingExpiredEvent(
		Long bookingId,
		UUID bookingUuid
) {}

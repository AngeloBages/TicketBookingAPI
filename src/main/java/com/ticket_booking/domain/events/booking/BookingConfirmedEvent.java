package com.ticket_booking.domain.events.booking;

import java.util.UUID;

public record BookingConfirmedEvent(
			Long bookingId,
			UUID bookingUuid
) {}

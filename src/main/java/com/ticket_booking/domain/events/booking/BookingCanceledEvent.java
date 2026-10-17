package com.ticket_booking.domain.events.booking;

import java.util.UUID;

public record BookingCanceledEvent(
		Long bookingId,
		UUID bookingUuid
) {}

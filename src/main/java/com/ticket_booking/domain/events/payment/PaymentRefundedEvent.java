package com.ticket_booking.domain.events.payment;

import java.util.UUID;

public record PaymentRefundedEvent(
			Long paymentId,
			UUID paymentUuid,
			Long bookingId
) {}

package com.ticket_booking.domain.events.payment;

import java.util.UUID;

public record PaymentCapturedEvent(
			Long paymentId,
			UUID paymentUuid,
			Long bookingId
) {}

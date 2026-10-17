package com.ticket_booking.domain.events.payment;

import java.util.UUID;

public record PaymentFailedEvent(
			Long paymentId,
			UUID paymentUuid,
			String failureCode,
			String failureMessage
) {}

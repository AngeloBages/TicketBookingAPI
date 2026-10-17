package com.ticket_booking.domain.events.payment;

import java.util.UUID;

public record PaymentOutcomeUnknownEvent(
			Long paymentId,
			UUID paymentUuid
) {}

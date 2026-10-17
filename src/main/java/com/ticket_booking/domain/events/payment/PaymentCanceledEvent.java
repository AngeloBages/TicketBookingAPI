package com.ticket_booking.domain.events.payment;

import java.util.UUID;

public record PaymentCanceledEvent(
		Long paymentId,
		UUID paymentUuid
) {}

package com.ticket_booking.domain.events.refund;

import java.util.UUID;

public record RefundSucceededEvent(
		Long refundId,
		UUID refundUuid,
		Long paymentId
) {}

package com.ticket_booking.domain.events.refund;

import java.util.UUID;

public record RefundFailedEvent(
		Long refundId,
		UUID refundUuid,
		String failureCode,
		String failureMessage
) {}

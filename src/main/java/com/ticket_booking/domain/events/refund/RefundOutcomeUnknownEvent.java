package com.ticket_booking.domain.events.refund;

import java.util.UUID;

public record RefundOutcomeUnknownEvent(
		Long refundId,
		UUID refundUuid
) {}


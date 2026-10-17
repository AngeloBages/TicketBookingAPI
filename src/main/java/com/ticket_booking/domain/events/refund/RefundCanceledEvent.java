package com.ticket_booking.domain.events.refund;

import java.util.UUID;

public record RefundCanceledEvent(
		Long refundId,
		UUID refundUuid
) {}

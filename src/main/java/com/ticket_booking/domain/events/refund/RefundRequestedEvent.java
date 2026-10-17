package com.ticket_booking.domain.events.refund;

public record RefundRequestedEvent(
		Long refundId
) {}
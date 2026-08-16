package com.ticket_booking.event.repositories.views;

import java.math.BigDecimal;
import java.util.UUID;

import com.ticket_booking.domain.models.enums.EventSeatStatus;

public record EventSeatView(
		UUID seatId,
        int number,
        String row,
        EventSeatStatus status,
        BigDecimal price
) {}

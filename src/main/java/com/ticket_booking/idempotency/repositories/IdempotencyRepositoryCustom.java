package com.ticket_booking.idempotency.repositories;

import java.time.Instant;
import java.util.Optional;

import com.ticket_booking.domain.models.enums.IdempotencyOperation;

public interface IdempotencyRepositoryCustom {
	
	Optional<Long> insertIfAbsent(
			IdempotencyOperation operation,
			String identityId,
            String idempotencyKey,
            String requestHash,
            Instant expiresAt
    );
}

package com.ticket_booking.idempotency.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ticket_booking.domain.models.enums.IdempotencyOperation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class IdempotencyRepositoryCustomImpl implements IdempotencyRepositoryCustom{

	@PersistenceContext
    private EntityManager entityManager;
	
	@Override
	public Optional<Long> insertIfAbsent(
			IdempotencyOperation operation, 
			String identityId, 
			String idempotencyKey,
			String requestHash, 
			Instant expiresAt) {

		@SuppressWarnings("unchecked")
		List<Long> ids = entityManager
	            .createNativeQuery("""
	                INSERT INTO idempotency_record (
	                    operation,
	                    identity_id,
	                    idempotency_key,
	                    request_hash,
	                    status,
	                    created_at,
	                    expires_at
	                )
	                VALUES (
	            		:operation,
	            		:identityId,
	                    :idempotencyKey,
	                    :requestHash,
	                    'IN_PROGRESS',
	                    CURRENT_TIMESTAMP,
	                    :expiresAt
	                )
	                ON CONFLICT (operation, idempotency_key) DO NOTHING
	                RETURNING id
	                """)
	            .setParameter("operation", operation.name())
	            .setParameter("identityId", identityId)
	            .setParameter("idempotencyKey", idempotencyKey)
	            .setParameter("requestHash", requestHash)
	            .setParameter("expiresAt", expiresAt)
	            .getResultList();

	    return ids.stream().findFirst();
	}
}



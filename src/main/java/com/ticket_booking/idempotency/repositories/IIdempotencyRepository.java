package com.ticket_booking.idempotency.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.ticket_booking.domain.models.IdempotencyRecord;
import com.ticket_booking.domain.models.enums.IdempotencyOperation;

public interface IIdempotencyRepository
	extends JpaRepository<IdempotencyRecord, Long>,
	        IdempotencyRepositoryCustom {
	
	Optional<IdempotencyRecord> findByOperationAndIdentityIdAndIdempotencyKey(
	    IdempotencyOperation operation,
	    String identityId,
	    String idempotencyKey);
	
	@Modifying
	@Query("""
		DELETE FROM IdempotencyRecord i
		WHERE i.status = 'COMPLETED'
		  AND i.expiresAt <= :now
	""")
	int deleteExpiredCompleted(
	    Instant now);
}

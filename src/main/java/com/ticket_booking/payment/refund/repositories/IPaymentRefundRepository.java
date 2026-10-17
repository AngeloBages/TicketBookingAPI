package com.ticket_booking.payment.refund.repositories;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;

import jakarta.persistence.LockModeType;

public interface IPaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
	
	Optional<PaymentRefund> findByUuid(UUID uuid);
	
	@Query("""
		SELECT r
		FROM PaymentRefund r
		JOIN FETCH r.payment
		WHERE r.id = :refundId
	""")
	Optional<PaymentRefund> findByIdWithPayment(Long refundId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT r
		FROM PaymentRefund r
		WHERE r.id = :id
	""")
	Optional<PaymentRefund> findByIdForUpdate(@Param("id") Long id);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT r
		FROM PaymentRefund r
		WHERE r.uuid = :uuid
	""")
	Optional<PaymentRefund> findByUuidForUpdate(@Param("uuid") UUID uuid);
	
	@Query("""
		SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
		FROM PaymentRefund r
		WHERE r.payment.id = :paymentId
		  AND r.status IN (:statuses)
	""")
	boolean hasActiveRefund(
			@Param("paymentId") Long paymentId,
			@Param("statuses") Collection<RefundStatus> statuses);
	
	@Query(value = """
		SELECT *
		FROM payment_refund
		WHERE status IN (:statuses)
		  AND (
			  reconciliation_claimed_at IS NULL
			  OR reconciliation_claimed_at < :claimExpiredBefore
			  )
		ORDER BY updated_at
		LIMIT :limit
		FOR UPDATE SKIP LOCKED
	""", nativeQuery = true)
	List<PaymentRefund> findRefundsForReconciliation(
			Set<String> statuses,
			Instant claimExpiredBefore,
			int limit);
    
    @Query(value = """
    	SELECT *
    	FROM payment_refund
    	WHERE status = 'PENDING'
    	  AND provider_refund_id IS NULL
    	  AND created_at < :staleBefore
    	  AND (
    	      reconciliation_claimed_at IS NULL
    	      OR reconciliation_claimed_at < :claimExpiredBefore
    	      )
    	ORDER BY created_at
    	FOR UPDATE SKIP LOCKED
    	LIMIT :limit
    """, nativeQuery = true)
    List<PaymentRefund> findStalePendingRefundsForUpdate(
    		Instant staleBefore,
    		Instant claimExpiredBefore,
    		int limit);
}

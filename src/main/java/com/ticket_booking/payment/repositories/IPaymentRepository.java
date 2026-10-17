package com.ticket_booking.payment.repositories;

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

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.domain.models.enums.PaymentStatus;

import jakarta.persistence.LockModeType;

public interface IPaymentRepository extends JpaRepository<Payment, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p
		FROM Payment p
		WHERE p.id = :id
	""")
	Optional<Payment> findByIdForUpdate(@Param("id") Long id);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p
		FROM Payment p
		WHERE p.uuid = :uuid
	""")
	Optional<Payment> findByUuidForUpdate(@Param("uuid") UUID uuid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p
		FROM Payment p
		WHERE p.provider = :provider
		  AND p.providerPaymentId = :providerPaymentId
	""")
	Optional<Payment> findByProviderAndProviderPaymentIdForUpdate(
			@Param("provider") PaymentProviderType provider,
		    @Param("providerPaymentId") String providerPaymentId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p
		FROM Payment p
		WHERE p.booking.uuid = :bookingUuid
		  AND p.status = 'CAPTURED'
		  AND p.booking.user.id = :userId
	    ORDER BY p.createdAt DESC
	""")
	Optional<Payment> findLatestCapturedPaymentByBookingUuidForUpdate(
			@Param("bookingUuid") UUID bookingUuid,
			@Param("userId") Long userId);
	
	@Query("""
			SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END
			FROM Payment p
			WHERE p.booking.id = :bookingId
			  AND p.status IN (:statuses)
		""")
		boolean hasActivePayment(
				@Param("bookingId") Long bookingId,
				@Param("statuses") Collection<PaymentStatus> statuses);
    
    @Query(value = """
    	SELECT *
		FROM payment
		WHERE status IN (:statuses)
    	  AND updated_at < :staleBefore
		  AND (
    		  reconciliation_claimed_at IS NULL
    		  OR reconciliation_claimed_at < :claimExpiredBefore
    		  )
		ORDER BY updated_at
		LIMIT :limit
		FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<Payment> findPaymentsForReconciliationForUpdate(
    		Set<String> statuses,
    		Instant staleBefore,
    		Instant claimExpiredBefore,
    		int limit);
    
    @Query(value = """
    	SELECT *
    	FROM payment
    	WHERE status = 'PENDING'
    	  AND provider_payment_id IS NULL
    	  AND provider_checkout_session_id IS NULL
    	  AND created_at < :staleBefore
    	  AND (
    	      reconciliation_claimed_at IS NULL
    	      OR reconciliation_claimed_at < :claimExpiredBefore
    	      )
    	ORDER BY created_at
    	FOR UPDATE SKIP LOCKED
    	LIMIT :limit
    """, nativeQuery = true)
    List<Payment> findStalePendingPaymentsForUpdate(
    		Instant staleBefore,
    		Instant claimExpiredBefore,
    		int limit);
}

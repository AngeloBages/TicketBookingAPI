package com.ticket_booking.payment.webhook.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.PaymentWebhook;
import com.ticket_booking.domain.models.enums.PaymentProviderType;


public interface IPaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    Optional<PaymentWebhook> findByProviderAndProviderEventId(
            PaymentProviderType provider,
            String providerEventId);
    
    @Modifying
    @Query(value = """
        INSERT INTO payment_webhook (
            provider,
            provider_event_id,
            type,
            payload,
            status,
            received_at,
            updated_at
        )
        VALUES (
            :provider,
            :providerEventId,
            :type,
            :payload,
            'PENDING',
            :receivedAt,
            :updatedAt
        )
        ON CONFLICT (provider, provider_event_id)
        DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("provider") String provider,
            @Param("providerEventId") String providerEventId,
            @Param("type") String type,
            @Param("payload") String payload,
            @Param("receivedAt") Instant receivedAt,
            @Param("updatedAt") Instant updatedAt
    );
    
    @Query(value = """
    	SELECT *
		FROM payment_webhook
		WHERE status = 'PENDING'
		ORDER BY received_at
		FOR UPDATE SKIP LOCKED
		LIMIT :limit
    	""", nativeQuery = true)
    List<PaymentWebhook> findPendingWebhooks(
    		int limit);
    
    @Query("""
    	UPDATE PaymentWebhook pw
		SET pw.status = 'PENDING'
		WHERE pw.status = 'PROCESSING' 
		  AND pw.updatedAt < :now
    """)
    @Modifying
    int requeueStaleProcessingWebhooks(Instant now);
}

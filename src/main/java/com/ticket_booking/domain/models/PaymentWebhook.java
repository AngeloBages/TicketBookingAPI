package com.ticket_booking.domain.models;

import java.time.Instant;
import java.util.Objects;

import com.ticket_booking.domain.models.enums.PaymentWebhookStatus;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.domain.models.valueobjects.FailureMessage;
import com.ticket_booking.payment.webhook.exceptions.InvalidPaymentWebhookStateException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "payment_webhook",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_provider_provider_event_id",
			columnNames = {"provider", "provider_event_id"}
		)
	}
)
public class PaymentWebhook {
	
	@Id
	@GeneratedValue(
		strategy = GenerationType.SEQUENCE, 
		generator = "payment_webhook_seq"
	)
	@SequenceGenerator(
		name = "payment_webhook_seq", 
		sequenceName = "payment_webhook_seq_generator"
	)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false)
	private PaymentProviderType provider;
	
	@Column(name = "provider_event_id", nullable = false, length = 225)
	private String providerEventId;
	
	@Column(nullable = false, length = 100)
	private String type;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentWebhookStatus status;
	
	@Column(length = 500)
	private String failureMessage;
	
	@Column(nullable = false, updatable = false)
	private Instant receivedAt;
	
	@Column(nullable = false)
	private Instant updatedAt;
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = Instant.now();
	}
	
	protected PaymentWebhook() {
		// JPA only
	}
	
	private PaymentWebhook(
			PaymentProviderType provider,
			String providerEventId,
			String type,
			String payload) {
		
		this.provider = Objects.requireNonNull(provider);
		this.providerEventId = Objects.requireNonNull(providerEventId);
		this.type = Objects.requireNonNull(type);
		this.payload = Objects.requireNonNull(payload);
		this.status = PaymentWebhookStatus.PENDING;
		this.receivedAt = Instant.now();
		this.updatedAt = this.receivedAt;
	}

	public static PaymentWebhook create(
	        PaymentProviderType provider,
	        String providerEventId,
	        String type,
	        String payload) {

	    return new PaymentWebhook(
	    		provider,
	    		providerEventId,
	    		type,
	    		payload);
	}

	public void flagForProcessing() {
		if (status != PaymentWebhookStatus.PENDING) {
			throw new InvalidPaymentWebhookStateException(
	        	"Payment webhook cannot be marked as processing from status: " + status
	        );
	    }
		this.status = PaymentWebhookStatus.PROCESSING;
	}
	
	public void markProcessed() {
		if (status != PaymentWebhookStatus.PROCESSING) {
			throw new InvalidPaymentWebhookStateException(
	        	"Payment webhook cannot be marked as processed from status: " + status
	        );
	    }
		this.status = PaymentWebhookStatus.PROCESSED;
	}

	public void markFailed(String message) {
		if (status != PaymentWebhookStatus.PROCESSING) {
			throw new InvalidPaymentWebhookStateException(
	        	"Payment webhook cannot be marked as failed from status: " + status
	        );
	    }
		this.status = PaymentWebhookStatus.FAILED;
		this.failureMessage = FailureMessage.of(failureMessage).value();
	}
	
	public boolean isflaggedForProcessing() {
	    return status == PaymentWebhookStatus.PROCESSING;
	}
	
	public boolean isProcessed() {
	    return status == PaymentWebhookStatus.PROCESSED;
	}
	
	public boolean isFailed() {
	    return status == PaymentWebhookStatus.FAILED;
	}
	
	
	public Long getId() {
		return id;
	}

	public PaymentProviderType getProvider() {
		return provider;
	}

	public String getProviderEventId() {
		return providerEventId;
	}

	public String getType() {
		return type;
	}
	
	public String getPayload() {
		return payload;
	}
	
	public PaymentWebhookStatus getStatus() {
		return status;
	}
	
	public String getFailureMessage() {
	    return failureMessage;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}
	
	public Instant getUpdatedAt() {
		return updatedAt;
	}
}

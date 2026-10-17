package com.ticket_booking.domain.models;

import java.time.Instant;
import java.util.Objects;

import com.ticket_booking.domain.models.enums.IdempotencyOperation;
import com.ticket_booking.domain.models.enums.IdempotencyStatus;
import com.ticket_booking.idempotency.exceptions.InvalidIdempotencyStateException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "idempotency_record",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_idempotency_operation_key",
            columnNames = {
                "operation",
                "idempotency_key"
            }
        )
    },
    indexes = {
        @Index(
            name = "ix_idempotency_expires_at",
            columnList = "expires_at"
        )
    }
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idempotency_seq")
	@SequenceGenerator(name = "idempotency_seq", sequenceName = "idempotency_seq_generator")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private IdempotencyOperation operation;

    @Column(
    	name = "identity_id",
        updatable = false
    )
    private String identityId;

    @Column(
        name = "idempotency_key",
        nullable = false,
        updatable = false,
        length = 255
    )
    private String idempotencyKey;

    @Column(
        name = "request_hash",
        nullable = false,
        updatable = false,
        length = 64
    )
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(
    	name = "expires_at", 
    	nullable = false, 
    	updatable = false
    )
    private Instant expiresAt;

    @Column
    private Instant completedAt;

    protected IdempotencyRecord() {
    	//JPA only
    }

    
    private IdempotencyRecord(
            IdempotencyOperation operation,
            String identityId,
            String idempotencyKey,
            String requestHash,
            Instant expiresAt) {

        this.operation = Objects.requireNonNull(operation);
        this.identityId = identityId;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.requestHash = Objects.requireNonNull(requestHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);

        this.status = IdempotencyStatus.IN_PROGRESS;
        this.createdAt = Instant.now();
    }

    public static IdempotencyRecord create(
            IdempotencyOperation operation,
            String identityId,
            String idempotencyKey,
            String requestHash,
            Instant expiresAt) {

        return new IdempotencyRecord(
                operation,
                identityId,
                idempotencyKey,
                requestHash,
                expiresAt
        );
    }
    
    public void complete(String responseBody) {

        if (status != IdempotencyStatus.IN_PROGRESS) {
            throw new InvalidIdempotencyStateException(
                "Idempotency record cannot be completed from status: " + status
            );
        }

        this.responseBody = Objects.requireNonNull(responseBody);
        this.status = IdempotencyStatus.COMPLETED;
        this.completedAt = Instant.now();
    }
    
    public boolean isInProgress() {
    	return status == IdempotencyStatus.IN_PROGRESS;
    }
    
    public boolean isCompleted() {
    	return status == IdempotencyStatus.COMPLETED;
    }
    
    public boolean hasSameRequest(String requestHash) {
        return this.requestHash.equals(requestHash);
    }
    
    public Long getId() {
    	return id;
    }
    
	public IdempotencyOperation getOperation() {
		return operation;
	}

	public String getIdentityId() {
		return identityId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getRequestHash() {
		return requestHash;
	}

	public IdempotencyStatus getStatus() {
		return status;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}
}

package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.domain.events.refund.RefundCanceledEvent;
import com.ticket_booking.domain.events.refund.RefundFailedEvent;
import com.ticket_booking.domain.events.refund.RefundOutcomeUnknownEvent;
import com.ticket_booking.domain.events.refund.RefundRequestedEvent;
import com.ticket_booking.domain.events.refund.RefundSucceededEvent;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.domain.models.valueobjects.FailureCode;
import com.ticket_booking.domain.models.valueobjects.FailureMessage;
import com.ticket_booking.payment.refund.exceptions.InvalidRefundStateException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "payment_refund",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_refund_provider_provider_refund_id",
			columnNames = {"provider", "provider_refund_id"}
		)
	}
)
public class PaymentRefund extends AggregateRoot {

	@Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "payment_refund_seq"
    )
    @SequenceGenerator(
        name = "payment_refund_seq",
        sequenceName = "payment_refund_seq_generator"
    )
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProviderType provider;

    @Column(name = "provider_refund_id", length = 225)
    private String providerRefundId;

    @Column(
    	nullable = false, 
    	updatable = false, 
    	precision = 10, 
    	scale = 2
    )
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

	@Column(length = 100)
	private String failureCode;
	
	@Column(length = 500)
	private String failureMessage;

	@Column
	private Instant reconciliationClaimedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
    	name = "payment_id", 
    	nullable = false, 
    	updatable = false
    )
    private Payment payment;
    
    @PreUpdate
	public void onUpdate() {
        this.updatedAt = Instant.now();
	}
	
    protected PaymentRefund() {
    	//JPA only
    }
    
    
    private PaymentRefund(
    		Payment payment) {
    	
        this.uuid = UUID.randomUUID();
        this.payment = Objects.requireNonNull(payment);
        
        payment.ensureRefundable();
        
        this.provider = payment.getProvider();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.status = RefundStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static PaymentRefund create(
            Payment payment) {

        return new PaymentRefund(
                payment
        );
    }
    
    public void requestProcessing() {
    	if (status != RefundStatus.PENDING) {
            throw new InvalidRefundStateException(
                "Refund request processing cannot be initiated from status: " + status
            );
        }
    	
    	registerEvent(new RefundRequestedEvent(id));
    }
    
    public void attachProviderRefundId(String providerRefundId) {
    	Objects.requireNonNull(providerRefundId);

        if (this.providerRefundId == null) {
            this.providerRefundId = providerRefundId;
            return;
        }

        if (!this.providerRefundId.equals(providerRefundId)) {
            throw new InvalidRefundStateException(
                "A different provider refund ID is already associated"
            );
        }
    }

    public void markProcessing() {
        if (status == RefundStatus.PROCESSING) {
            return;
        }

        if (status != RefundStatus.PENDING) {
            throw new InvalidRefundStateException(
                "Refund cannot be marked as processing from status: " + status
            );
        }

        status = RefundStatus.PROCESSING;
    }
    
    public void markRequiresAction() {
    	if (status == RefundStatus.REQUIRES_ACTION) {
            return;
        }
    	
    	if (status != RefundStatus.PROCESSING) {
            throw new InvalidRefundStateException(
                "Refund cannot be marked as requires action from status: " + status
            );
        }
        this.status = RefundStatus.REQUIRES_ACTION;
    }
    
    public void succeed() {
        if (status == RefundStatus.SUCCEEDED) {
            return;
        }

        if (status != RefundStatus.PROCESSING &&
            status != RefundStatus.UNKNOWN) {
            throw new InvalidRefundStateException(
                "Refund cannot succeed from status: " + status
            );
        }

        status = RefundStatus.SUCCEEDED;
        
        registerEvent(
        		new RefundSucceededEvent(
        				id,
        				uuid,
        				payment.getId()
        		)
        );
    }

    public void fail(
    		String errorCode,
    		String message) {
        if (status == RefundStatus.FAILED) {
            return;
        }

        if (status != RefundStatus.PROCESSING &&
        	status != RefundStatus.UNKNOWN) {
            throw new InvalidRefundStateException(
                "Refund cannot fail from status: " + status
            );
        }

        status = RefundStatus.FAILED;
        failureCode = FailureCode.of(failureCode).value();
        failureMessage = FailureMessage.of(failureMessage).value();
        
        registerEvent(
        		new RefundFailedEvent(
        				id,
        				uuid,
        				failureCode,
        				failureMessage
        		)
        );
    }
    
    public void cancel() {
    	if (status == RefundStatus.CANCELED) {
            return;
        }
    	
    	if (status != RefundStatus.PROCESSING &&
    		status != RefundStatus.REQUIRES_ACTION &&
            status != RefundStatus.UNKNOWN) {
        	throw new InvalidRefundStateException(
            	"Refund cannot be canceled from status: " + status
        	);
    	}
        this.status = RefundStatus.CANCELED;
        
        registerEvent(
        		new RefundCanceledEvent(
        				id,
        				uuid
        		)
        );
    }
    
    public void markUnknown() {
        if (status == RefundStatus.UNKNOWN) {
            return;
        }

        if (status != RefundStatus.PROCESSING) {
            throw new InvalidRefundStateException(
                "Refund cannot be marked as unknown from status: " + status
            );
        }

        status = RefundStatus.UNKNOWN;
        
        registerEvent(
        		new RefundOutcomeUnknownEvent(
        				id,
        				uuid
        		)
        );
    }
    
    public void setReconciliationClaimedAt(Instant now) {
    	this.reconciliationClaimedAt = now;
    }
    
    public void clearReconciliationTimestamp() {
    	this.reconciliationClaimedAt = null;
    }
    
    public Long getId() {
		return id;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getFailureCode() {
		return failureCode;
	}
	
	public String getFailureMessage() {
		return failureMessage;
	}

	public Payment getPayment() {
        return payment;
    }
	
	public Long getPaymentId() {
		return payment.getId();
	}

    public PaymentProviderType getProvider() {
        return provider;
    }

    public String getProviderRefundId() {
        return providerRefundId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

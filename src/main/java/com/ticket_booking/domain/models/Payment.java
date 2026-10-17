package com.ticket_booking.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.ticket_booking.domain.events.payment.PaymentCanceledEvent;
import com.ticket_booking.domain.events.payment.PaymentCapturedEvent;
import com.ticket_booking.domain.events.payment.PaymentFailedEvent;
import com.ticket_booking.domain.events.payment.PaymentOutcomeUnknownEvent;
import com.ticket_booking.domain.events.payment.PaymentRefundedEvent;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.domain.models.valueobjects.FailureCode;
import com.ticket_booking.domain.models.valueobjects.FailureMessage;
import com.ticket_booking.domain.models.valueobjects.PaymentAmount;
import com.ticket_booking.domain.models.valueobjects.PaymentCurrency;
import com.ticket_booking.payment.exceptions.InvalidPaymentStateException;

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
	name = "payment",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_provider_provider_payment_id",
			columnNames = {"provider", "provider_payment_id"}
		)
	}
)
public class Payment extends AggregateRoot {

	@Id
	@GeneratedValue(
		strategy = GenerationType.SEQUENCE, 
		generator = "payment_seq"
	)
	@SequenceGenerator(
		name = "payment_seq", 
		sequenceName = "payment_seq_generator"
	)
	private Long id;
	
	@Column(nullable = false, unique = true, updatable = false)
	private UUID uuid;
	
	@Column(nullable = false, precision = 10, scale = 2, updatable = false)
	private BigDecimal amount;
	
	@Column(nullable = false, length = 3, updatable = false)
	private String currency;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false)
	private PaymentProviderType provider;
	
	@Column(name = "provider_payment_id", length = 225)
	private String providerPaymentId;
	
	@Column(length = 225)
	private String providerCheckoutSessionId;
	
	@Column(length = 100)
	private String failureCode;

	@Column(length = 500)
	private String failureMessage;

	@Column
	private Instant reconciliationClaimedAt;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
		name = "booking_id",
		nullable = false,
		updatable = false
	)
	private Booking booking;
	
	@PreUpdate
	public void onUpdate() {
        this.updatedAt = Instant.now();
	}
	
	protected Payment() {
		// JPA only
	}
	
	
	private Payment(
			Booking booking,
			PaymentAmount amount,
			PaymentCurrency currency,
			PaymentProviderType provider,
			Instant now) {

		
        this.uuid = UUID.randomUUID();
        this.booking = Objects.requireNonNull(booking);

        booking.ensurePayable(now);
        
        this.amount = amount.value();
        this.currency = currency.value();
        this.provider = Objects.requireNonNull(provider);
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
	}
	
	public static Payment create(
			Booking booking,
			BigDecimal amount,
			String currency,
			PaymentProviderType provider,
			Instant now) {
		
		return new Payment(
				booking,
				new PaymentAmount(amount),
				new PaymentCurrency(currency),
				provider,
				now);
	}
	
    public void attachProviderPaymentId(String providerPaymentId) {
    	Objects.requireNonNull(providerPaymentId);

        if (this.providerPaymentId == null) {
            this.providerPaymentId = providerPaymentId;
            return;
        }

        if (!this.providerPaymentId.equals(providerPaymentId)) {
            throw new InvalidPaymentStateException(
                "A different provider payment ID is already associated"
            );
        }
    }
    
    public void attachProviderCheckoutSessionId(String providerCheckoutSessionId) {
    	Objects.requireNonNull(providerCheckoutSessionId);
    	
        if (this.providerCheckoutSessionId == null) {
        	this.providerCheckoutSessionId = providerCheckoutSessionId;
        	return;
        }
        
        if (!this.providerCheckoutSessionId.equals(providerCheckoutSessionId)) {
            throw new InvalidPaymentStateException(
                "A differenct provider checkout ID is already associated"
            );
        }
    }
    
    public void markProcessing() {
    	if (status == PaymentStatus.PROCESSING) {
            return;
        }
    	
    	if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                "Payment cannot be marked as processing from status: " + status
            );
        }
        this.status = PaymentStatus.PROCESSING;
    }
    
    public void markRequiresAction() {
    	if (status == PaymentStatus.REQUIRES_ACTION) {
            return;
        }
    	
    	if (status != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException(
                "Payment cannot be marked as requires action from status: " + status
            );
        }
        this.status = PaymentStatus.REQUIRES_ACTION;
    }

    public void authorize() {
    	if (status == PaymentStatus.AUTHORIZED) {
            return;
        }
    	
    	if (status != PaymentStatus.PROCESSING &&
    		status != PaymentStatus.REQUIRES_ACTION) {
            throw new InvalidPaymentStateException(
                "Payment cannot be authorized from status: " + status
            );
        }
        this.status = PaymentStatus.AUTHORIZED;
    }
    
    public void capture() {
    	if (status == PaymentStatus.CAPTURED) {
            return;
        }
    	
    	if (status != PaymentStatus.PROCESSING &&
        	status != PaymentStatus.REQUIRES_ACTION &&
    		status != PaymentStatus.AUTHORIZED &&
            status != PaymentStatus.UNKNOWN) {
            throw new InvalidPaymentStateException(
            		"Payment cannot be captured from status: " + status
            );
        }
        this.status = PaymentStatus.CAPTURED;
        
        registerEvent(
        		new PaymentCapturedEvent(
        				id,
        				uuid,
        				booking.getId()
        		)
        );
    }
    
    public void markFailed(String code, String message) {
    	if (status == PaymentStatus.FAILED) {
            return;
        }
    	
    	if (status != PaymentStatus.PENDING &&
    		status != PaymentStatus.PROCESSING &&
        	status != PaymentStatus.REQUIRES_ACTION &&
            status != PaymentStatus.UNKNOWN) {
            throw new InvalidPaymentStateException(
            	"Payment cannot be marked as failed from status: " + status
            );
        }
        status = PaymentStatus.FAILED;
        failureCode = FailureCode.of(failureCode).value();
        failureMessage = FailureMessage.of(failureMessage).value();
        
        registerEvent(
        		new PaymentFailedEvent(
        				id,
        				uuid,
        				failureCode,
        				failureMessage
        		)
        );
    }
    
    public void cancel() {
    	if (status == PaymentStatus.CANCELED) {
            return;
        }
    	
    	if (status != PaymentStatus.PENDING &&
    		status != PaymentStatus.PROCESSING &&
    		status != PaymentStatus.REQUIRES_ACTION &&
            status != PaymentStatus.UNKNOWN) {
        	throw new InvalidPaymentStateException(
            	"Payment cannot be canceled from status: " + status
        	);
    	}
        this.status = PaymentStatus.CANCELED;
        
        registerEvent(
        		new PaymentCanceledEvent(
        				id,
        				uuid
        		)
        );
    }
    
    public void markUnknown() {
    	if (status == PaymentStatus.UNKNOWN) {
            return;
        }
    	
    	if (status != PaymentStatus.PENDING &&
    	    status != PaymentStatus.PROCESSING) {
        	throw new InvalidPaymentStateException(
            	"Payment cannot be marked as unknown from status: " + status
        	);
    	}
        this.status = PaymentStatus.UNKNOWN;
        
        registerEvent(
        		new PaymentOutcomeUnknownEvent(
        				id,
        				uuid
        		)
        );
    }
    
    public void markRefunded() {
    	if (status == PaymentStatus.REFUNDED) {
            return;
        }
    	
    	if (status != PaymentStatus.CAPTURED) {
        	throw new InvalidPaymentStateException(
            	"Payment cannot be marked as refunded from status: " + status
        	);
    	}
        this.status = PaymentStatus.REFUNDED;
        
        registerEvent(
        		new PaymentRefundedEvent(
        				id,
        				uuid,
        				booking.getId()
        		)
        );
    }
    
    public void setReconciliationClaimedAt(Instant now) {
    	this.reconciliationClaimedAt = now;
    }
    
    public void clearReconciliationTimestamp() {
    	this.reconciliationClaimedAt = null;
    }
    
    public void ensureRefundable() {
    	if (status != PaymentStatus.CAPTURED) {
	        throw new InvalidPaymentStateException(
	            "Only a captured payment can be refunded"
	        );
	    }
    }

	public Long getId() {
		return id;
	}

	public UUID getUuid() {
		return uuid;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public PaymentProviderType getProvider() {
		return provider;
	}

	public String getProviderPaymentId() {
		return providerPaymentId;
	}
	
	public String getProviderCheckoutSessionId() {
		return providerCheckoutSessionId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Booking getBooking() {
		return booking;
	}
	
	public Long getBookingId() {
		return booking.getId();
	}
}

package com.ticket_booking.payment.stripe.utils;

import java.math.BigDecimal;
import java.util.UUID;

import com.stripe.model.Event;
import com.stripe.model.MetadataStore;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.ticket_booking.common.exceptions.enums.IntegrationFailureType;
import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import static com.ticket_booking.payment.PaymentProviderMetadata.*;

import com.ticket_booking.payment.stripe.enums.StripePaymentIntentStatus;
import com.ticket_booking.payment.stripe.enums.StripeRefundStatus;
import com.ticket_booking.payment.stripe.enums.StripeSessionPaymentStatus;
import com.ticket_booking.payment.stripe.exceptions.InvalidStripePaymentStatusException;
import com.ticket_booking.payment.stripe.exceptions.InvalidStripeRefundStatusException;
import com.ticket_booking.payment.stripe.exceptions.InvalidStripeWebhookException;

public final class StripeUtils {

	private StripeUtils() {}
	
	public static StripeSessionPaymentStatus mapSessionPaymentStatus(Session session) {
		return StripeSessionPaymentStatus.from(session.getPaymentStatus())
			        .orElseThrow(
			                () -> new InvalidStripeRefundStatusException(
			                        "Unexpected Stripe Session payment status: "
			                                + session.getStatus()
			                )
			        );
	}
	
	public static StripePaymentIntentStatus mapPaymentIntentStatus(PaymentIntent paymentIntent) {
		return StripePaymentIntentStatus.from(paymentIntent.getStatus())
			        .orElseThrow(
			                () -> new InvalidStripePaymentStatusException(
			                        "Unexpected Stripe PaymentIntent status: "
			                                + paymentIntent.getStatus()
			                )
			        );
	}
	
	public static StripeRefundStatus mapRefundStatus(Refund refund) {
		return StripeRefundStatus.from(refund.getStatus())
			        .orElseThrow(
			                () -> new InvalidStripeRefundStatusException(
			                        "Unexpected Stripe Refund status: "
			                                + refund.getStatus()
			                )
			        );
	}
	
	public static Session getSession(Event event) {
    	return (Session) event
				.getDataObjectDeserializer()
				.getObject()
				.orElseThrow(() -> 
						invalidWebhook(
								"Unable to deserialize Checkout Session", 
								null
						)
				);
    }
    
	public static PaymentIntent getPaymentIntent(Event event) {
    	return (PaymentIntent) event
                .getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() ->
                        invalidWebhook(
                                "Unable to deserialize PaymentIntent",
                                null
                        )
                );
    }
	
	public static Refund getRefund(Event event) {
    	return (Refund) event
    			.getDataObjectDeserializer()
    			.getObject()
    			.orElseThrow(() ->
    					invalidWebhook(
    							"Unable to deserialize Stripe Refund",
    							null
    					)
    			);
    }
	
	public static String requirePaymentIntent(Session session) {

        String paymentIntentId =
                session.getPaymentIntent();

        if (paymentIntentId == null ||
                paymentIntentId.isBlank()) {

            throw invalidWebhook(
                    "Stripe Checkout Session does not contain a PaymentIntent",
                    null
            );
        }

        return paymentIntentId;
    }
	
	public static <T> UUID requirePaymentUuid(MetadataStore<T> object) {
        String paymentId = object.getMetadata() == null
                ? null
                : object.getMetadata().get(PAYMENT_ID);

        if (paymentId == null || paymentId.isBlank()) {
            throw invalidWebhook(
                    "Stripe resource is missing payment_id metadata",
                    null
            );
        }

        try {
            return UUID.fromString(paymentId);
        } catch (IllegalArgumentException ex) {
            throw invalidWebhook(
                    "Invalid payment_id metadata",
                    ex
            );
        }
    }

	public static UUID requireRefundUuid(Refund refund) {
    	String refundId = refund.getMetadata() == null
    			? null
    			: refund.getMetadata().get(REFUND_ID);

    	if (refundId == null || refundId.isBlank()) {
    		throw invalidWebhook(
    				"Stripe Refund is missing refund_id metadata",
    				null
    		);
    	}

    	try {
    		return UUID.fromString(refundId);
    	} catch (IllegalArgumentException ex) {
    		throw invalidWebhook(
    				"Invalid refund_id metadata",
    				ex
    		);
    	}
    }
	
	public static long amountInMinorUnits(BigDecimal amount) {
	    return amount
	            .movePointRight(2)
	            .longValueExact();
	}
	
	public static String getFailureCode(PaymentIntent paymentIntent) {
		return paymentIntent.getLastPaymentError() != null 
				? paymentIntent.getLastPaymentError().getCode()
				: null;
	}

	public static String getFailureMessage(PaymentIntent paymentIntent) {
		return paymentIntent.getLastPaymentError() != null 
				? paymentIntent.getLastPaymentError().getMessage()
				: null;
	}

    public static InvalidStripeWebhookException invalidWebhook(
    		String message,
    		Throwable cause) {
    	return new InvalidStripeWebhookException(
    			IntegrationFailureType.PROVIDER_ERROR,
    			OperationOutcome.FAILED,
    			message,
    			cause
    	);
    }
}

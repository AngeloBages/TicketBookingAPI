package com.ticket_booking.payment.stripe;

import static com.ticket_booking.payment.PaymentProviderMetadata.PAYMENT_ID;
import static com.ticket_booking.payment.PaymentProviderMetadata.REFUND_ID;

import java.util.List;
import java.util.UUID;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeCollection;
import com.stripe.model.StripeSearchResult;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentSearchParams;
import com.stripe.param.RefundListParams;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.exceptions.ExternalResourceNotFoundException;
import com.ticket_booking.payment.stripe.utils.RetryPredicate;
import com.ticket_booking.payment.stripe.utils.StripeExceptionTranslator;

@Component
public class StripeRetrieveOperations {

	private final StripeClient stripeClient;
	
	public StripeRetrieveOperations(
			StripeClient stripeClient) {
		this.stripeClient = stripeClient;
	}
	
	@Retryable(
		predicate = RetryPredicate.class,
		maxRetriesString = "${app.payment.provider.retry.max-retries}",
		delayString = "${app.payment.provider.retry.delay}",
		jitterString = "${app.payment.provider.retry.jitter}",
		multiplierString = "${app.payment.provider.retry.multiplier}",
		maxDelayString = "${app.payment.provider.retry.max-delay}"
	)
	@ConcurrencyLimit(limitString = "${app.payment.provider.concurrency-limit}")
	public PaymentIntent retrievePaymentIntent(
			String paymentIntentId) {
		
		try {
	        return stripeClient
                  .v1()
                  .paymentIntents()
                  .retrieve(paymentIntentId);

	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }
	}
		
	@Retryable(
		predicate = RetryPredicate.class,
		maxRetriesString = "${app.payment.provider.retry.max-retries}",
		delayString = "${app.payment.provider.retry.delay}",
		jitterString = "${app.payment.provider.retry.jitter}",
		multiplierString = "${app.payment.provider.retry.multiplier}",
		maxDelayString = "${app.payment.provider.retry.max-delay}"
	)
	@ConcurrencyLimit(limitString = "${app.payment.provider.concurrency-limit}")
	public Session retrieveCheckoutSession(
			String checkoutSessionId) {
		
		try {
	        return stripeClient
	               	.v1()
	                .checkout()
	                .sessions()
	                .retrieve(checkoutSessionId);

	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }
	}
		
	@Retryable(
		predicate = RetryPredicate.class,
		maxRetriesString = "${app.payment.provider.retry.max-retries}",
		delayString = "${app.payment.provider.retry.delay}",
		jitterString = "${app.payment.provider.retry.jitter}",
		multiplierString = "${app.payment.provider.retry.multiplier}",
		maxDelayString = "${app.payment.provider.retry.max-delay}"
	)
	@ConcurrencyLimit(limitString = "${app.payment.provider.concurrency-limit}")
	public PaymentIntent findPaymentIntentByLocalPaymentId(
	        UUID paymentUuid) {
		
		String query = """
				metadata["%s"]:"%s"
				"""
				.formatted(PAYMENT_ID, paymentUuid);

	    PaymentIntentSearchParams params =
	            PaymentIntentSearchParams.builder()
	                    .setQuery(query)
	                    .setLimit(1L)
	                    .build();

	    try {

	        StripeSearchResult<PaymentIntent> result =
	                stripeClient
	                        .v1()
	                        .paymentIntents()
	                        .search(params);

	        List<PaymentIntent> paymentIntents = result.getData();

	        if (paymentIntents.isEmpty()) {
	        	throw new ExternalResourceNotFoundException(
	    	    		PaymentProviderType.STRIPE.toString(),
	    	    		"Payment Intent",
	    	            "Stripe payment intent could not be found for local payment",
	    	            null
	    	    );
	        }

	        return paymentIntents.get(0);

	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }
	}
		
	@Retryable(
		predicate = RetryPredicate.class,
		maxRetriesString = "${app.payment.provider.retry.max-retries}",
		delayString = "${app.payment.provider.retry.delay}",
		jitterString = "${app.payment.provider.retry.jitter}",
		multiplierString = "${app.payment.provider.retry.multiplier}",
		maxDelayString = "${app.payment.provider.retry.max-delay}"
	)
	@ConcurrencyLimit(limitString = "${app.payment.provider.concurrency-limit}")
	public Refund retrieveRefund(String refundId) {
		try {
	        return stripeClient
	                  .v1()
	                  .refunds()
	                  .retrieve(refundId);

	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }
	}
			
	@Retryable(
		predicate = RetryPredicate.class,
		maxRetriesString = "${app.payment.provider.retry.max-retries}",
		delayString = "${app.payment.provider.retry.delay}",
		jitterString = "${app.payment.provider.retry.jitter}",
		multiplierString = "${app.payment.provider.retry.multiplier}",
		maxDelayString = "${app.payment.provider.retry.max-delay}"
	)
	@ConcurrencyLimit(limitString = "${app.payment.provider.concurrency-limit}")
	public Refund findRefundByLocalUuid(
	        UUID refundUuid,
	        String providerPaymentId) {

	    RefundListParams params =
	            RefundListParams.builder()
	                    .setPaymentIntent(providerPaymentId)
	                    .setLimit(10L)
	                    .build();
	    
	    StripeCollection<Refund> refunds = null;

	    try {
		    refunds =
		            stripeClient.v1()
		                    .refunds()
		                    .list(params);
		    
	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }

	    for (Refund refund : refunds.getData()) {

	        String metadataRefundId =
	                refund.getMetadata().get(REFUND_ID);

	        if (refundUuid.toString().equals(metadataRefundId)) {
	            return refund;
	        }
	    }
	    
	    throw new ExternalResourceNotFoundException(
	    		PaymentProviderType.STRIPE.toString(),
	    		"Refund",
	            "Stripe refund could not be found for local refund",
	            null
	    );
	}
}

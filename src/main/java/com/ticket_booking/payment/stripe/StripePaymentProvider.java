package com.ticket_booking.payment.stripe;

import static com.ticket_booking.payment.PaymentProviderMetadata.BOOKING_ID;
import static com.ticket_booking.payment.PaymentProviderMetadata.PAYMENT_ID;
import static com.ticket_booking.payment.PaymentProviderMetadata.REFUND_ID;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData;
import com.stripe.param.checkout.SessionCreateParams.Mode;
import com.stripe.param.checkout.SessionCreateParams.PaymentIntentData;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.IPaymentProvider;
import com.ticket_booking.payment.ProviderCheckoutStatus;
import com.ticket_booking.payment.ProviderPaymentStatus;
import com.ticket_booking.payment.commands.PaymentProviderCommands.CancelProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.CreateProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RefundProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderPaymentStatusCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderRefundStatusCommand;
import com.ticket_booking.payment.exceptions.ExternalResourceNotFoundException;
import com.ticket_booking.payment.exceptions.PaymentCheckoutClosedException;
import com.ticket_booking.payment.refund.ProviderRefundStatus;
import com.ticket_booking.payment.responses.PaymentProviderResponses.CreatePaymentResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.CheckoutSessionResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.PaymentStatusResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundPaymentResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundStatusResult;
import com.ticket_booking.payment.stripe.enums.StripePaymentIntentStatus;
import com.ticket_booking.payment.stripe.enums.StripeRefundStatus;
import com.ticket_booking.payment.stripe.enums.StripeSessionPaymentStatus;
import com.ticket_booking.payment.stripe.enums.StripeSessionStatus;
import com.ticket_booking.payment.stripe.utils.RetryPredicate;
import com.ticket_booking.payment.stripe.utils.StripeExceptionTranslator;
import com.ticket_booking.payment.stripe.utils.StripeUtils;

import io.micrometer.observation.annotation.Observed;

@Component
public class StripePaymentProvider implements IPaymentProvider {

	private final StripeClient stripeClient;
	private final StripeProperties properties;
	private final StripeRetrieveOperations stripeRetrieve;
	
	public StripePaymentProvider(
			StripeClient stripeClient,
			StripeProperties properties,
			StripeRetrieveOperations stripeRetrieve) {
		this.stripeClient = stripeClient;
		this.properties = properties;
		this.stripeRetrieve = stripeRetrieve;
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
	@Observed(
        name = "provider.payment.create",
        contextualName = "create-payment-provider"
    )
	@Override
	public CreatePaymentResult createPayment(CreateProviderPaymentCommand command) {

		SessionCreateParams.Builder builder = 
				SessionCreateParams.builder()
						.setMode(Mode.PAYMENT)
						.setSuccessUrl(properties.successUrl() + "?session_id={CHECKOUT_SESSION_ID}")
						.setCancelUrl(properties.cancelUrl())
						.putMetadata(PAYMENT_ID, command.paymentUuid().toString())
						.putMetadata(BOOKING_ID, command.bookingUuid().toString())
						.addAllPaymentMethodType(
								List.of(
										SessionCreateParams.PaymentMethodType.CARD,
										SessionCreateParams.PaymentMethodType.PIX
								)
						);
		
		PaymentIntentData paymentIntentData = 
				SessionCreateParams.PaymentIntentData.builder()
						.putMetadata(PAYMENT_ID, command.paymentUuid().toString())
						.putMetadata(BOOKING_ID, command.bookingUuid().toString())
		                .build();

		builder.setPaymentIntentData(paymentIntentData);
		
		
		ProductData productData = 
				SessionCreateParams.LineItem.PriceData.ProductData.builder()
						.setName(command.productName())
						.setDescription(command.productDescription())
						.build();
				
		PriceData priceData = 
				SessionCreateParams.LineItem.PriceData.builder()
						.setCurrency(command.currency())
						.setUnitAmount(StripeUtils.amountInMinorUnits(command.amount()))
						.setProductData(productData)
						.build();
		
		LineItem lineItem = 
				SessionCreateParams.LineItem.builder()
						.setPriceData(priceData)
						.setQuantity(1L)
						.build();
	
		builder.addLineItem(lineItem);
				
		SessionCreateParams params = builder.build();
		
		try {
			RequestOptions options = RequestOptions.builder()
					.setIdempotencyKey(command.idempotencyKey())
					.build();
			
			Session session = 
					stripeClient
						.v1()
						.checkout()
						.sessions()
						.create(params, options);
			
			return new CreatePaymentResult(
					session.getId(),
					session.getUrl()
			);
			
		} catch(StripeException ex) {
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
	@Observed(
        name = "provider.refund.create",
        contextualName = "create-provider-refund"
    )
	@Override
	public RefundPaymentResult refundPayment(RefundProviderPaymentCommand command) {
		
		RefundCreateParams params = 
				RefundCreateParams.builder()
						.setPaymentIntent(command.providerPaymentId())
			            .setAmount(StripeUtils.amountInMinorUnits(command.amount()))
			            .putMetadata(REFUND_ID, command.refundUuid().toString())
			            .putMetadata(PAYMENT_ID, command.paymentUuid().toString())
			            .build();

		RequestOptions options = 
				RequestOptions.builder()
						.setIdempotencyKey(command.idempotencyKey())
						.build();

		Refund refund = null;
		
	    try {
	        refund = stripeClient
	                .v1()
	                .refunds()
	                .create(params, options);

	    } catch (StripeException ex) {
	        throw StripeExceptionTranslator.translate(ex);
	    }
	    
	    StripeRefundStatus status = StripeUtils.mapRefundStatus(refund);
	    
	    return switch (status) {
			case PENDING ->
					new RefundPaymentResult(
							refund.getId(),
							ProviderRefundStatus.PROCESSING,
							null,
							null
					);

			case REQUIRES_ACTION ->
					new RefundPaymentResult(
							refund.getId(),
							ProviderRefundStatus.REQUIRES_ACTION,
							null,
							null
					);
					
			case SUCCEEDED ->
					new RefundPaymentResult(
							refund.getId(),
							ProviderRefundStatus.SUCCEEDED,
							null,
							null
					);
			
			case FAILED ->
					new RefundPaymentResult(
							refund.getId(),
							ProviderRefundStatus.FAILED,
							refund.getFailureReason(),
							null
					);
					
			case CANCELED ->
					new RefundPaymentResult(
							refund.getId(),
							ProviderRefundStatus.CANCELED,
							null,
							null
					);
	    };
	}

	@Override
	public void cancelPayment(CancelProviderPaymentCommand command) {
		// TODO Auto-generated method stub
		
	}

	@Observed(
        name = "provider.payment.retrieve",
        contextualName = "retrieve-provider-payment-status"
	)
	@Override
	public PaymentStatusResult retrievePaymentStatus(RetrieveProviderPaymentStatusCommand command) {

		PaymentIntent paymentIntent = null;
		Session session = null;
		
		try {
			paymentIntent = resolvePaymentIntent(command);
		} catch (ExternalResourceNotFoundException ex) {
			session = 
					stripeRetrieve
						.retrieveCheckoutSession(
								command.providerCheckoutSessionId()
						);
		}
		
		StripePaymentIntentStatus status = null;
		
		if(paymentIntent == null) {
			StripeSessionPaymentStatus paymentStatus = 
					StripeUtils.mapSessionPaymentStatus(session);
			
			if(paymentStatus == StripeSessionPaymentStatus.PAID) {
				status = StripePaymentIntentStatus.SUCCEEDED;
				
			} else if (paymentStatus == StripeSessionPaymentStatus.UNPAID) {
				status = StripePaymentIntentStatus.PROCESSING;
			}
			
		} else {
			status = StripeUtils.mapPaymentIntentStatus(paymentIntent);
			
		}
	    
	    return switch (status) {

	        case SUCCEEDED ->
	                new PaymentStatusResult(
	                        ProviderPaymentStatus.SUCCEEDED,
	                        null,
	                        null
	                );

	        case CANCELED ->
	                new PaymentStatusResult(
	                        ProviderPaymentStatus.CANCELED,
	                        null,
	                        null
	                );
	                
	        case REQUIRES_ACTION ->
		            new PaymentStatusResult(
		                    ProviderPaymentStatus.REQUIRES_ACTION,
		                    null,
		                    null
		            );
		
		    case PROCESSING,
		         REQUIRES_PAYMENT_METHOD,
		         REQUIRES_CONFIRMATION ->
		            new PaymentStatusResult(
		                    ProviderPaymentStatus.PROCESSING,
		                    null,
		                    null
		            );
		
		    case REQUIRES_CAPTURE ->
		            throw new IllegalStateException(
		                    "Stripe PaymentIntent unexpectedly requires capture: "
		                            + paymentIntent.getId()
		            );
	    };
	}
	
	@Observed(
        name = "provider.refund.retrieve",
        contextualName = "retrieve-provider-refund-status"
	)
	@Override
	public RefundStatusResult retrieveRefundStatus(RetrieveProviderRefundStatusCommand command) {

		Refund refund = resolveRefund(command);
		
		if (refund == null) {
		    return new RefundStatusResult(
                    ProviderRefundStatus.PROCESSING,
                    null,
                    null
            );
		}

	    StripeRefundStatus status = 
	    		StripeUtils.mapRefundStatus(refund);
	    
	    return switch (status) {

	        case SUCCEEDED ->
	                new RefundStatusResult(
	                        ProviderRefundStatus.SUCCEEDED,
	                        null,
	                        null
	                );

	        case FAILED ->
	                new RefundStatusResult(
	                        ProviderRefundStatus.FAILED,
	                        refund.getFailureReason(),
	                        null
	                );

	        case CANCELED ->
	                new RefundStatusResult(
	                        ProviderRefundStatus.CANCELED,
	                        null,
	                        null
	                );
	               
	        case REQUIRES_ACTION ->
	            new RefundStatusResult(
	                    ProviderRefundStatus.REQUIRES_ACTION,
	                    null,
	                    null
	            );

	        case PENDING -> 
		        new RefundStatusResult(
	                    ProviderRefundStatus.PROCESSING,
	                    null,
	                    null
	            );
	    };
	}
	
	@Override
	@Observed(
        name = "provider.checkout.session.retrieve",
        contextualName = "retrieve-provider-checkout-session"
	)
	public CheckoutSessionResult retrieveCheckoutSession(String providerCheckoutSessionId) {

		Session session =
				stripeRetrieve.
					retrieveCheckoutSession(
							providerCheckoutSessionId
					);
		
		StripeSessionStatus status = 
	    		StripeUtils.mapSessionStatus(session);

		return switch (status) {

			case OPEN ->
				new CheckoutSessionResult(
						session.getId(),
						ProviderCheckoutStatus.OPEN,
						session.getUrl()
				);
				
			case COMPLETE ->
				new CheckoutSessionResult(
						session.getId(),
						ProviderCheckoutStatus.COMPLETE,
						null
				);
			
			case EXPIRED ->
				new CheckoutSessionResult(
						session.getId(),
						ProviderCheckoutStatus.EXPIRED,
						null
				);
		};
	}
	
	@Override
	@Observed(
        name = "provider.checkout.session.expire",
        contextualName = "expire-provider-checkout-session"
	)
	public void expireCheckoutSession(String providerCheckoutSessionId) {
		Session session =
				stripeRetrieve.
					retrieveCheckoutSession(
							providerCheckoutSessionId
					);
		
		StripeSessionStatus status = 
	    		StripeUtils.mapSessionStatus(session);
		
		try {
			if(status != StripeSessionStatus.OPEN) {
				throw new PaymentCheckoutClosedException();
			}
			
			session = session.expire();
			
		} catch(StripeException ex) {
			throw StripeExceptionTranslator.translate(ex);
		}
	}

	@Override
	public PaymentProviderType getProviderType() {
		return PaymentProviderType.STRIPE;
	}
	
	private PaymentIntent resolvePaymentIntent(RetrieveProviderPaymentStatusCommand command) {

		if(Strings.isNotBlank(command.providerPaymentId())) {
			return stripeRetrieve.retrievePaymentIntent(
					command.providerPaymentId()
			);
		}
		
		if(Strings.isNotBlank(command.providerCheckoutSessionId())) {
			var session = 
					stripeRetrieve
						.retrieveCheckoutSession(
								command.providerCheckoutSessionId()
						);
			
			String paymentIntentId = session.getPaymentIntent();
			
			if(Strings.isNotBlank(paymentIntentId)) {
				return stripeRetrieve
						.retrievePaymentIntent(paymentIntentId);
			}
		}
		
		return stripeRetrieve
				.findPaymentIntentByLocalPaymentId(command.paymentUuid());
	}
	
	private Refund resolveRefund(RetrieveProviderRefundStatusCommand command) {

	    if (Strings.isNotBlank(command.providerRefundId())) {
	        return stripeRetrieve
	        		.retrieveRefund(command.providerRefundId());
	    }

	    return stripeRetrieve.findRefundByLocalUuid(
	            command.refundUuid(),
	            command.providerPaymentId()
	    );
	}
}

package com.ticket_booking.payment.refund;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.DomainEventDispatcher;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;
import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.RefundStatus;
import com.ticket_booking.payment.PaymentProviderResolver;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RefundProviderPaymentCommand;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.exceptions.PaymentProviderException;
import com.ticket_booking.payment.refund.exceptions.RefundAlreadyInProgressException;
import com.ticket_booking.payment.refund.repositories.IPaymentRefundRepository;
import com.ticket_booking.payment.repositories.IPaymentRepository;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundPaymentResult;

@Service
public class PaymentRefundService {
	
	private static final Logger log =
            LoggerFactory.getLogger(PaymentRefundService.class);
	
	private final IPaymentRefundRepository refundRepository;
	private final IPaymentRepository paymentRepository;
	private final RefundStateService refundStateService;
	private final PaymentProviderResolver paymentProviderResolver;
	private final DomainEventDispatcher dispatcher;
	
	public PaymentRefundService(
			IPaymentRefundRepository refundRepository,
			IPaymentRepository paymentRepository,
			RefundStateService refundStateService,
			PaymentProviderResolver paymentProviderResolver,
			DomainEventDispatcher dispatcher) {
		this.refundRepository = refundRepository;
		this.paymentRepository = paymentRepository;
		this.refundStateService = refundStateService;
		this.paymentProviderResolver = paymentProviderResolver;
		this.dispatcher = dispatcher;
	}
	
	@Transactional
	public PaymentRefund createRefund(Long paymentId) {
		Payment payment = paymentRepository
				.findByIdForUpdate(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException());
		
		return createRefund(payment);
	}

	@Transactional
	public PaymentRefund createRefund(UUID bookingId, Long userId) {
		Payment payment = paymentRepository
				.findLatestCapturedPaymentByBookingUuidForUpdate(
						bookingId,
						userId
				)
				.orElseThrow(() -> new PaymentNotFoundException());
		
		return createRefund(payment);
	}

	private PaymentRefund createRefund(Payment payment) {
		
		if (refundRepository.hasActiveRefund(
    			payment.getId(),
    			RefundStatus.getActiveStatus())) {
	        throw new RefundAlreadyInProgressException();
	    }
		
		PaymentRefund refund = 
				PaymentRefund.create(payment);

		refund = refundRepository.save(refund);
		
		refund.requestProcessing();
		dispatcher.publishEventsFor(refund);
		
		return refund;
	}

	public RefundStatus processRefund(Long refundId) {
		 PaymentRefund refund = 
				 refundStateService.markProcessing(refundId);
		
		 Payment payment = paymentRepository
					.findById(refund.getPaymentId())
					.orElseThrow(() -> new PaymentNotFoundException());

	    try {
	    	log.info(
                "Creating provider refund refundtId={} provider={}",
                refund.getUuid(),
                refund.getProvider()
            );
	    	
	    	String providerIdempotencyKey =
	    	        "refund:" + refund.getUuid();
	    	
	    	RefundPaymentResult result = 
	    			paymentProviderResolver.resolve(payment.getProvider())
	    			.refundPayment(
	    					new RefundProviderPaymentCommand(
	    							refund.getUuid(),
	    							payment.getUuid(),
	    							payment.getProviderPaymentId(),
	    							refund.getAmount(),
	    							providerIdempotencyKey
	    					)
	    			);
	    	
	    	log.info(
	    	        "Provider refund request completed "
	    	                + "refundId={} provider={} providerRefundId={} "
	    	                + "status={} failureCode={}",
	    	        refund.getUuid(),
	    	        refund.getProvider(),
	    	        result.providerRefundId(),
	    	        result.status(),
	    	        result.failureCode()
	    	);

	    	return refundStateService
	    			.handleResultStatus(refundId, result);

	    } catch (PaymentProviderException ex) {
	    	log.info(
    			"Provider refund request failed refundId={} provider={}",
    			refund.getUuid(),
                refund.getProvider(),
                ex
        	);
	    	
	    	switch (ex.getOutcome()) {
	            case FAILED ->
	            		refundStateService.fail(
	                            refund.getId(),
	                            ex.getErrorCode(),
	                            IntegrationExceptionUtils
	                				.getSafeFailureMessage(
	                						ex.getFailureType(), 
	                						ex.getOutcome(),
	                						ex.getServiceName()
	                				)
	                    );
	
	            case UNKNOWN ->
	            		refundStateService
	            			.markOutcomeUnknown(refund.getId());
	        }
	    	
	    	throw ex;
	    }
	}
}

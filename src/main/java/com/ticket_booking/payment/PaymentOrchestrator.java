package com.ticket_booking.payment;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.PaymentRefund;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.commands.PaymentCommands.CapturePaymentCommand;
import com.ticket_booking.payment.commands.PaymentCommands.CreatePaymentCommand;
import com.ticket_booking.payment.commands.PaymentCommands.RefundPaymentCommand;
import com.ticket_booking.payment.commands.PaymentCommands.ResumeCheckoutCommand;
import com.ticket_booking.payment.exceptions.UnsupportedPaymentProviderException;
import com.ticket_booking.payment.recovery.PendingPaymentClaimer;
import com.ticket_booking.payment.recovery.PendingPaymentRecoveryService;
import com.ticket_booking.payment.refund.PaymentRefundService;
import com.ticket_booking.payment.responses.PaymentResponses.CreatePaymentResponse;
import com.ticket_booking.payment.responses.PaymentResponses.GetPaymentResponse;
import com.ticket_booking.payment.responses.PaymentResponses.RefundResponse;
import com.ticket_booking.payment.responses.PaymentResponses.ResumeCheckoutResponse;

import io.micrometer.observation.annotation.Observed;

@Service
public class PaymentOrchestrator {
	
	private final PendingPaymentClaimer pendingPaymentClaimer;
	private final PendingPaymentRecoveryService paymentRecoveryService;
    private final PaymentService paymentService;
    private final PaymentRefundService refundService;
    private final PaymentProperties paymentProperties;
    
    public PaymentOrchestrator(
    		PendingPaymentClaimer pendingPaymentClaimer,
    		PendingPaymentRecoveryService paymentRecoveryService,
            PaymentService paymentService,
            PaymentRefundService refundService,
            PaymentProperties paymentProperties) {

    	this.pendingPaymentClaimer = pendingPaymentClaimer;
    	this.paymentRecoveryService = paymentRecoveryService;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.paymentProperties = paymentProperties;
    }
    
    @Observed(
        name = "payment.create",
        contextualName = "create-payment"
    )
    public CreatePaymentResponse createPayment(CreatePaymentCommand command) {

    	PaymentProviderType providerType =
    			PaymentProviderType
	    			.from(paymentProperties.providerName())
	    			.orElseThrow(() -> 
	    					new UnsupportedPaymentProviderException(
	    							paymentProperties.providerName()
	    					)
	    			);
        
    	Payment payment = 
    			paymentService.createPayment(
    					command.userId(),
    					command.bookingId(),
    					paymentProperties.currency(),
    					providerType
    			);

    	var result = paymentService.processPayment(payment);

    	payment = 
    			paymentService.savePaymentProviderInfo(
		    			payment.getId(),
		    			result.checkoutSessionId()
		    	);      

    	return new CreatePaymentResponse(
    			payment.getUuid(),
    			result.redirectUrl()
    	);
    }
    
	public GetPaymentResponse getPayment(UUID paymentId) {
		throw new NotImplementedException();
	}

	public void capturePayment(CapturePaymentCommand command) {
		throw new NotImplementedException();
	}

	public void cancelPayment(UUID paymentId) {
		throw new NotImplementedException();
	}
	
	@Observed(
        name = "payment.refund.request",
        contextualName = "request-refund-by-booking"
	)
	public RefundResponse refundPayment(RefundPaymentCommand command) {
		
		PaymentRefund refund = 
				refundService.createRefund(
						command.bookingId(),
						command.userId()
				);

	    return new RefundResponse(
	            refund.getUuid(),
	            refund.getStatus()
	    );
	}
	
	@Observed(
        name = "payment.refund.request",
        contextualName = "request-refund-by-payment-id"
	)
	public RefundResponse refundPayment(Long paymentId) {
			
		PaymentRefund refund = 
				refundService.createRefund(
						paymentId
				);
		
		return new RefundResponse(
	            refund.getUuid(),
	            refund.getStatus()
	    );
	}
	
	@Observed(
        name = "payment.checkout.resume",
        contextualName = "resume-checkout-payment"
	)
	public ResumeCheckoutResponse resumeCheckout(ResumeCheckoutCommand command) {
		
		var resumeResult = 
				paymentService
					.resumeCheckout(
							command.paymentId(), 
							command.userId()
					);
		
		String redirectUrl = 
				switch (resumeResult.status()) {
				
					case OPENED ->
						resumeResult.redirectUrl();
						
					case NOT_STARTED -> {
						pendingPaymentClaimer
								.claimPendingPayment(
										resumeResult.paymentId()
								);
						
						var recoveryResult = paymentRecoveryService
								.recover(resumeResult.paymentId());
						
						yield recoveryResult.redirectUrl();
					}
				};
		
		return new ResumeCheckoutResponse(
				command.paymentId(),
				redirectUrl
		);	
	}
}

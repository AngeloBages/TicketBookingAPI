package com.ticket_booking.payment.recovery;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.ticket_booking.booking.exceptions.BookingAlreadyExpiredException;
import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.payment.PaymentService;
import com.ticket_booking.payment.exceptions.InvalidPaymentStateException;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.repositories.IPaymentRepository;
import com.ticket_booking.payment.responses.PaymentResponses.RecoverPaymentResult;

import io.micrometer.observation.annotation.Observed;

@Service
public class PendingPaymentRecoveryService {

    private final IPaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PendingPaymentRecoveryService(
            IPaymentRepository paymentRepository,
            PaymentService paymentService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Observed(
        name = "payment.recovery",
        contextualName = "recover-pending-payment"
	)
    public RecoverPaymentResult recover(Long paymentId) {
        Payment payment = paymentRepository
        		.findByIdWithBooking(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException());

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
            		"Payment can't be recovered from status: " + payment.getStatus()
            );
        }

        var result = paymentService.processPayment(payment);

		paymentService.savePaymentProviderInfo(
    			payment.getId(),
    			result.checkoutSessionId()
    	); 	
        
		try {
			payment.getBooking().ensureNotExpired(Instant.now());
			
		} catch (BookingAlreadyExpiredException ex) {
			paymentService.expireCheckoutSession(
					payment.getId(), 
					result.checkoutSessionId()
			);
			
			throw ex;
		}
		
		return new RecoverPaymentResult(
				result.checkoutSessionId(),
				result.redirectUrl()
		);
    }
}
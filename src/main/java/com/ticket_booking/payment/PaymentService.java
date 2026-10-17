package com.ticket_booking.payment;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.booking.exceptions.BookingNotFoundException;
import com.ticket_booking.booking.repositories.IBookingRepository;
import com.ticket_booking.booking.utils.BookingPaymentMapper;
import com.ticket_booking.common.exceptions.IntegrationExceptionUtils;
import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.Payment;
import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.domain.models.enums.PaymentStatus;
import com.ticket_booking.payment.commands.PaymentProviderCommands.CreateProviderPaymentCommand;
import com.ticket_booking.payment.exceptions.PaymentAlreadyInProgressException;
import com.ticket_booking.payment.exceptions.PaymentNotFoundException;
import com.ticket_booking.payment.exceptions.PaymentProviderException;
import com.ticket_booking.payment.repositories.IPaymentRepository;
import com.ticket_booking.payment.responses.PaymentProviderResponses.CreatePaymentResult;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);
	
	private final IPaymentRepository paymentRepository;
	private final IBookingRepository bookingRepository;
	private final PaymentStateService paymentStateService;
	private final PaymentProviderResolver paymentProviderResolver;
	
	public PaymentService(
			IPaymentRepository paymentRepository,
			IBookingRepository bookingRepository,
			PaymentStateService paymentStateService,
			PaymentProviderResolver paymentProviderResolver) {
		this.paymentRepository = paymentRepository;
		this.bookingRepository = bookingRepository;
		this.paymentStateService = paymentStateService;
		this.paymentProviderResolver = paymentProviderResolver;
	}

	@Transactional
    public Payment createPayment(
    		Long userId,
    		UUID bookingUuid,
    		String currency,
    		PaymentProviderType providerType) {
    	
    	Booking booking = findBookingForUpdate(
        		bookingUuid, 
        		userId
        );
    	
    	if (paymentRepository.hasActivePayment(
    			booking.getId(),
    			PaymentStatus.getActiveStatus())) {
	        throw new PaymentAlreadyInProgressException();
	    }

        Payment payment = Payment.create(
                booking,
                booking.getTotalPrice(),
                currency,
                providerType,
                Instant.now()
        );
        
        return paymentRepository.save(payment);
    }
	
	public CreatePaymentResult processPayment(
			Payment payment,
    		PaymentProviderType providerType) {
		
		Booking booking = 
				bookingRepository
						.findByIdWithDescriptionAssociations(payment.getBookingId())
    					.orElseThrow(() -> new BookingNotFoundException());

        try {
        	log.info(
                "Creating provider payment paymentId={} provider={}",
                payment.getUuid(),
                providerType
            );
    		
    		String providerIdempotencyKey =
        	        "payment:" + payment.getUuid();
        	
	        var result = paymentProviderResolver.resolve(providerType)
	        		.createPayment(
	        		new CreateProviderPaymentCommand(
	        				payment.getUuid(),
	        				booking.getUuid(),
	        				payment.getAmount(),
	        				payment.getCurrency()
	        					.toLowerCase(Locale.ROOT),
		        			BookingPaymentMapper.toProductName(booking),
	        				BookingPaymentMapper.toCheckoutDescription(booking),
	        				providerIdempotencyKey
	        		)
	        );
	        
	        log.info(
                "Provider payment created "
                		+ "paymentId={} provider={}",
                payment.getUuid(),
                providerType
            );
	        
	        return result;
        
        } catch (PaymentProviderException ex) {
        	log.info(
    			"Provider payment request failed paymentId={} provider={}",
    			payment.getUuid(),
                providerType,
    			ex
        	);
        	
        	switch (ex.getOutcome()) {
	            case FAILED ->
	                    paymentStateService.fail(
	                            payment.getId(),
	                            ex.getErrorCode(),
	                            IntegrationExceptionUtils
	                				.getSafeFailureMessage(
	                						ex.getFailureType(), 
	                						ex.getOutcome(),
	                						ex.getServiceName()
	                				)
	                    );
	
	            case UNKNOWN ->
	                    paymentStateService
	                    	.markOutcomeUnknown(payment.getId());
	        }
            throw ex;
        }
	}
	
	@Transactional
    public Payment savePaymentProviderInfo(
    		Long paymentId,
    		String checkoutSessionId) {
    	Payment payment = findPaymentForUpdate(paymentId);
    	
    	payment.attachProviderCheckoutSessionId(checkoutSessionId);
        
    	payment.markProcessing();
    	
    	return payment;
    }
    
    private Booking findBookingForUpdate(
    		UUID bookingId,
    		Long userId) {
    	
    	return bookingRepository
    			.findByUuidAndUserIdForUpdate(
    					bookingId,
    					userId
    			)
    			.orElseThrow(() -> new BookingNotFoundException());
    }
    
    private Payment findPaymentForUpdate(
			Long paymentId) {
		
		return paymentRepository
				.findByIdForUpdate(paymentId)
				.orElseThrow(
						() -> new PaymentNotFoundException()
				);
	}
}

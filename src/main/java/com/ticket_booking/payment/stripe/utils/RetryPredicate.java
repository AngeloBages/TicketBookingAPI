package com.ticket_booking.payment.stripe.utils;

import java.lang.reflect.Method;

import org.springframework.resilience.retry.MethodRetryPredicate;

import com.ticket_booking.common.exceptions.enums.OperationOutcome;
import com.ticket_booking.payment.exceptions.PaymentProviderException;

public final class RetryPredicate implements MethodRetryPredicate {
	@Override
	public boolean shouldRetry(Method method, Throwable throwable) {
		if(throwable instanceof PaymentProviderException ex) {
			return ex.getOutcome() == OperationOutcome.UNKNOWN;
		}
		return false;
	}
}

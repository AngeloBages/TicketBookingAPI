package com.ticket_booking.payment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.exceptions.UnsupportedPaymentProviderException;

@Component
public class PaymentProviderResolver {

	private final List<IPaymentProvider> providers;
	
	public PaymentProviderResolver(
			List<IPaymentProvider> providers) {
		this.providers = providers;
	}

    public IPaymentProvider resolve(PaymentProviderType paymentProvider) {

        return providers.stream()
                .filter(provider ->
                        provider.getProviderType() == paymentProvider)
                .findFirst()
                .orElseThrow(() ->
                        new UnsupportedPaymentProviderException(
                        		paymentProvider.name()
                        )
                );
    }
}

package com.ticket_booking.payment;

import com.ticket_booking.domain.models.enums.PaymentProviderType;
import com.ticket_booking.payment.commands.PaymentProviderCommands.CancelProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.CreateProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RefundProviderPaymentCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderPaymentStatusCommand;
import com.ticket_booking.payment.commands.PaymentProviderCommands.RetrieveProviderRefundStatusCommand;
import com.ticket_booking.payment.responses.PaymentProviderResponses.CreatePaymentResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.CheckoutSessionResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.PaymentStatusResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundPaymentResult;
import com.ticket_booking.payment.responses.PaymentProviderResponses.RefundStatusResult;

public interface IPaymentProvider {

	PaymentProviderType getProviderType();

	CreatePaymentResult createPayment(CreateProviderPaymentCommand command);

    RefundPaymentResult refundPayment(RefundProviderPaymentCommand command);

    void cancelPayment(CancelProviderPaymentCommand command);

    PaymentStatusResult retrievePaymentStatus(RetrieveProviderPaymentStatusCommand command);
    
    RefundStatusResult retrieveRefundStatus(RetrieveProviderRefundStatusCommand command);

	CheckoutSessionResult retrieveCheckoutSession(String providerCheckoutSessionId);

	void expireCheckoutSession(String providerCheckoutSessionId);
}


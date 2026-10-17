package com.ticket_booking.payment.stripe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;

@Configuration
public class StripeConfig {

    @Bean
    StripeClient stripeClient(StripeProperties properties) {
        return new StripeClient(properties.secretKey());
    }
}


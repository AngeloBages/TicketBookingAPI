package com.ticket_booking.payment.stripe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/payments/stripe")
@Tag(name = "Stripe Webhook")
public class StripeWebhookController {
	
	private final StripeWebhookHandler webhookService;
	
	public StripeWebhookController(
			StripeWebhookHandler service) {
		this.webhookService = service;
	}
	
	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(
			@RequestHeader("Stripe-Signature") String signature,
			@RequestBody String payload) {
		
		webhookService.handle(signature, payload);
		
		return ResponseEntity.ok().build();
	}
}

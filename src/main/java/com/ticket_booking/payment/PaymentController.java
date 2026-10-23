package com.ticket_booking.payment;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.payment.commands.PaymentCommands.ResumeCheckoutCommand;
import com.ticket_booking.payment.responses.PaymentResponses.ResumeCheckoutResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "Payment endpoints")
public class PaymentController {
	
	private final PaymentOrchestrator paymentOrchestrator;

	public PaymentController(
			PaymentOrchestrator paymentOrchestrator) {
		this.paymentOrchestrator = paymentOrchestrator;
	}
	
	@PostMapping("/{id}/checkout-session")
	@Operation(summary = "Resumes a payment checkout")
	public ResumeCheckoutResponse resumeCheckout(
			@AuthenticationPrincipal(expression = "user.id") Long userId,
			@PathVariable UUID id) {
		
		return paymentOrchestrator.resumeCheckout(
				new ResumeCheckoutCommand(
						userId,
						id
				)
		);
	}
}

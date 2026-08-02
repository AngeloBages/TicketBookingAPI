package com.ticket_booking.common.openapi;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@SecurityScheme(
	    name = "Bearer Authentication",
	    type = SecuritySchemeType.HTTP,
	    scheme = "bearer",
	    bearerFormat = "JWT"
)
@OpenAPIDefinition(
	info = @Info(
		title = "Ticket Booking API",
		version = "1.0.0",
		description = "REST API for the Ticket Booking application."
	),
	servers = {
		@Server(url = "http://localhost:8080", description = "Local")
	},
	security = {
		@SecurityRequirement(name = "Bearer Authentication")
	}
)
public class OpenApiConfiguration {

}

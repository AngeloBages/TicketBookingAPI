package com.ticket_booking.common.security.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties (
	String jwtSecret,
	@DefaultValue("2h") Duration accessTokenExpiration,
    @DefaultValue("7d") Duration refreshTokenExpiration
) { }

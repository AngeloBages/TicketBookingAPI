package com.ticket_booking.common.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties (
	String jwtSecret,
	@DefaultValue("7200000") long accessTokenExpiration,     // 2 hours in ms
    @DefaultValue("604800000") long refreshTokenExpiration   // 7 days in ms
    ) { }

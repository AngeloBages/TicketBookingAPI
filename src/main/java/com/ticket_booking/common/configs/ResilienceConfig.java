package com.ticket_booking.common.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;


@Configuration(proxyBeanMethods = false)
@EnableResilientMethods
public class ResilienceConfig {

}

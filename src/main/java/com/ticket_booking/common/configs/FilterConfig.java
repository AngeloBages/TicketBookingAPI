package com.ticket_booking.common.configs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ticket_booking.common.web.HttpAccessLogFilter;

@Configuration(proxyBeanMethods = false)
public class FilterConfig {

    @Bean
    FilterRegistrationBean<HttpAccessLogFilter> httpAccessLogFilterRegistration() {
        var registrationBean = new FilterRegistrationBean<HttpAccessLogFilter>();
        
        registrationBean.setFilter(new HttpAccessLogFilter());
        registrationBean.addUrlPatterns("/api/*");
        
        return registrationBean;
    }
}
package com.ticket_booking.common.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ticket_booking.auth.JwtService;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
    		UserDetailsService userDetailsService,
    		JwtService jwtService,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {
    	

        this.jwtAuthenticationFilter = 
        		new JwtAuthenticationFilter(
        				userDetailsService,
        				jwtService
        		);

        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }
    
    @Order(1)
    @Bean
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll() 
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
            )
            .httpBasic(withDefaults());

        return http.build();
    }
	
    @Order(2)
	@Bean 
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http
			.csrf(csrf -> csrf.disable())
	
			.sessionManagement(session ->
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			
			.authorizeHttpRequests(auth -> auth
				    // Public endpoints
				    .requestMatchers("/api/public/**").permitAll()
				    .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
				    .requestMatchers(HttpMethod.GET, "/api/venues/**").permitAll()
				    .requestMatchers(HttpMethod.POST, "/api/payments/*/webhook").permitAll()

				    // Admin endpoints
				    .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ADMIN")
				    .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
				    .requestMatchers(HttpMethod.PATCH, "/api/events/**").hasRole("ADMIN")
				    .requestMatchers(HttpMethod.POST, "/api/venues").hasRole("ADMIN")
				    .requestMatchers(HttpMethod.PUT, "/api/venues/**").hasRole("ADMIN")
				    .requestMatchers(HttpMethod.DELETE, "/api/venues/**").hasRole("ADMIN")
				    .requestMatchers("/api/admin/**").hasRole("ADMIN")
				    
				    // Swagger endpoints
				    .requestMatchers(
							"/swagger-ui/**",
			                "/swagger-ui.html",
			                "/v3/api-docs/**",
			                "/v3/api-docs.yaml",
			                "/swagger/**",
			                "/swagger.html",
			                "/api-docs/**",
			                "/api-docs.yaml")
					.permitAll()

				    // Secure default
				    .anyRequest().authenticated()
			)
	
			.exceptionHandling(ex -> ex
					.authenticationEntryPoint(authenticationEntryPoint)
					.accessDeniedHandler(accessDeniedHandler))
	
			.addFilterBefore(
					jwtAuthenticationFilter,
					UsernamePasswordAuthenticationFilter.class);
	
			return http.build();
	}
	
	@Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
	
	@Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

package com.ticket_booking.common.web;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class HttpAccessLogFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(HttpAccessLogFilter.class);
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs =
                    (System.nanoTime() - startedAt) / 1_000_000;

            log.info(
                "HTTP {} {} completed status={} durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs
            );
        }
    }
}

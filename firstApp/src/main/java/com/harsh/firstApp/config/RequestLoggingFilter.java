package com.harsh.firstApp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Request/response logging filter for audit trail and observability.
 * Logs method, URI, status code, and duration for every request.
 * Adds a correlation ID (X-Request-ID) for distributed tracing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);

        logger.info("→ {} {} {} ip={}", method, uri,
                queryString != null ? "?" + queryString : "", clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                logger.error("← {} {} status={} duration={}ms", method, uri, status, duration);
            } else if (status >= 400) {
                logger.warn("← {} {} status={} duration={}ms", method, uri, status, duration);
            } else {
                logger.info("← {} {} status={} duration={}ms", method, uri, status, duration);
            }

            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip health checks and actuator to reduce noise
        return path.startsWith("/actuator") || path.equals("/");
    }
}

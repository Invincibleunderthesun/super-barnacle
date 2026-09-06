package com.harsh.uday.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory IP-based rate limiter using Bucket4j.
 * Auth endpoints: 5 req/min, General API: 60 req/min.
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        String path = httpReq.getRequestURI();
        String clientIP = getClientIP(httpReq);

        // Skip rate limiting for non-API paths
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket;
        if (path.startsWith("/api/v1/auth/")) {
            // Stricter limit for auth endpoints (5 req/min)
            bucket = authBuckets.computeIfAbsent(clientIP, k ->
                    Bucket.builder().addLimit(Bandwidth.classic(5,
                            Refill.intervally(5, Duration.ofMinutes(1)))).build());
        } else {
            // General limit (60 req/min)
            bucket = apiBuckets.computeIfAbsent(clientIP, k ->
                    Bucket.builder().addLimit(Bandwidth.classic(60,
                            Refill.intervally(60, Duration.ofMinutes(1)))).build());
        }

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpRes.setStatus(429);
            httpRes.setContentType("application/json");
            httpRes.setHeader("Retry-After", "60");
            httpRes.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"errorCode\":\"RATE_LIMITED\"}");
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

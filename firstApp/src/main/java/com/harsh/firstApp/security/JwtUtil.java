package com.harsh.firstApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility class for token generation, validation, and parsing.
 * Secret key is required via environment/properties — no hardcoded defaults.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private SecretKey secretKey;
    private long expirationTime;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET is not configured or is too short (min 32 characters). " +
                    "Set the JWT_SECRET environment variable or jwt.secret property.");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = jwtExpiration;
        logger.info("JWT configured: expiration={}ms", expirationTime);
    }

    /**
     * Generate JWT token with email and role claims
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate token and return email (subject)
     */
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get expiration time in seconds
     */
    public long getExpirationTime() {
        return expirationTime / 1000;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
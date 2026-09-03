package com.harsh.firstApp.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for unauthorized access (401).
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("Authentication required", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}

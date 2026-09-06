package com.harsh.uday.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for forbidden access (403).
 */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}

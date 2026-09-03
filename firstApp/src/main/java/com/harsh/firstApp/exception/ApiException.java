package com.harsh.firstApp.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for API errors with HTTP status support.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "BAD_REQUEST";
    }

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = status.name();
    }

    public ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

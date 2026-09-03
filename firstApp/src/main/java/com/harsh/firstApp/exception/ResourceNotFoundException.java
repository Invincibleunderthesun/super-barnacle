package com.harsh.firstApp.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for resource not found scenarios (404).
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id),
                HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(String.format("%s not found with %s: %s", resourceName, field, value),
                HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}

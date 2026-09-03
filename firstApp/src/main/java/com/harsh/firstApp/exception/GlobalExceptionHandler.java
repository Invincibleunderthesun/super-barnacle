package com.harsh.firstApp.exception;

import com.harsh.firstApp.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.StaleObjectStateException;

/**
 * Global exception handler for centralized error handling.
 * Provides consistent error response format across all endpoints.
 * IMPORTANT: Never leak internal exception messages to the client in production.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle custom API exceptions
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex, HttpServletRequest request) {
        logger.error("API Exception: {} at {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation failed for request to {}: {}", request.getRequestURI(), errors);

        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Validation failed");
        response.setData(errors);
        response.setPath(request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle bad credentials (authentication failure)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        logger.warn("Authentication failed: {} at {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Invalid email or password",
                "BAD_CREDENTIALS",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied (authorization failure)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied: {} at {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(
                "Access denied. You don't have permission to access this resource.",
                "ACCESS_DENIED",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle optimistic locking conflicts
     */
    @ExceptionHandler(StaleObjectStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(
            StaleObjectStateException ex, HttpServletRequest request) {
        logger.warn("Optimistic lock conflict at {}: {}", request.getRequestURI(), ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "This record was modified by another user. Please refresh and try again.",
                "CONFLICT",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle missing required request parameters
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Missing parameter at {}: {}", request.getRequestURI(), ex.getParameterName());

        ApiResponse<Void> response = ApiResponse.error(
                "Missing required parameter: " + ex.getParameterName(),
                "MISSING_PARAMETER",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle runtime exceptions — DO NOT LEAK internal details to client
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        logger.error("Runtime exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Return generic message — never expose internal error details
        ApiResponse<Void> response = ApiResponse.error(
                "An error occurred while processing your request.",
                "RUNTIME_ERROR",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions — DO NOT LEAK internal details to client
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

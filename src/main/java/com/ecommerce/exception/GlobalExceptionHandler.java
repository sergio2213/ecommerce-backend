package com.ecommerce.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ecommerce.dto.ErrorDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorDetails> buildResponse(HttpStatus status, String message) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
        return new ResponseEntity<>(errorDetails, status);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDetails> handleNoSuchElementException(NoSuchElementException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found: " + ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDetails> handleIllegalStateException(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDetails> handleConflict(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "The resource already exists or violates a constraint.");
    }
}

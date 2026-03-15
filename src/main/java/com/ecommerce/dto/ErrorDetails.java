package com.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetails(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    Map<String, String> validationErrors
) {
    public ErrorDetails(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
    ) {
        this(timestamp, status, error, message, null);
    }
}

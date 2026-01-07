package com.rappidrive.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error response DTO for API exceptions.
 */
@Schema(description = "Error response with details")
public record ErrorResponse(
    
    @Schema(description = "Error timestamp", example = "2026-01-03T10:00:00")
    @JsonProperty("timestamp")
    LocalDateTime timestamp,
    
    @Schema(description = "HTTP status code", example = "404")
    @JsonProperty("status")
    int status,
    
    @Schema(description = "Error type", example = "Not Found")
    @JsonProperty("error")
    String error,
    
    @Schema(description = "Error message", example = "Driver not found with id: 123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("message")
    String message,
    
    @Schema(description = "Request path", example = "/api/v1/drivers/123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("path")
    String path,
    
    @Schema(description = "Validation errors (if applicable)")
    @JsonProperty("validationErrors")
    List<ValidationError> validationErrors
) {
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
    
    @Schema(description = "Field validation error")
    public record ValidationError(
        @JsonProperty("field")
        String field,
        
        @JsonProperty("message")
        String message
    ) {}
}

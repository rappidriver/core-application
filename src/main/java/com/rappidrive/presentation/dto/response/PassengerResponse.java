package com.rappidrive.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for passenger information.
 */
@Schema(description = "Passenger information")
public record PassengerResponse(
    
    @Schema(description = "Passenger ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    UUID id,
    
    @Schema(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174001")
    @JsonProperty("tenantId")
    UUID tenantId,
    
    @Schema(description = "Passenger's full name", example = "Maria Santos")
    @JsonProperty("fullName")
    String fullName,
    
    @Schema(description = "Passenger's email", example = "maria.santos@example.com")
    @JsonProperty("email")
    String email,
    
    @Schema(description = "Passenger's phone (formatted)", example = "+55 11 98765-4321")
    @JsonProperty("phone")
    String phone,
    
    @Schema(description = "Passenger status", example = "ACTIVE")
    @JsonProperty("status")
    String status,
    
    @Schema(description = "Creation timestamp", example = "2026-01-03T10:00:00")
    @JsonProperty("createdAt")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp", example = "2026-01-03T10:30:00")
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt
) {}

package com.rappidrive.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rappidrive.presentation.dto.common.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for driver information.
 */
@Schema(description = "Driver information")
public record DriverResponse(
    
    @Schema(description = "Driver ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    UUID id,
    
    @Schema(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174001")
    @JsonProperty("tenantId")
    UUID tenantId,
    
    @Schema(description = "Driver's full name", example = "João da Silva")
    @JsonProperty("fullName")
    String fullName,
    
    @Schema(description = "Driver's email", example = "joao.silva@example.com")
    @JsonProperty("email")
    String email,
    
    @Schema(description = "Driver's CPF (formatted)", example = "123.456.789-09")
    @JsonProperty("cpf")
    String cpf,
    
    @Schema(description = "Driver's phone (formatted)", example = "+55 11 98765-4321")
    @JsonProperty("phone")
    String phone,
    
    @Schema(description = "Driver status", example = "ACTIVE")
    @JsonProperty("status")
    String status,
    
    @Schema(description = "Driver's current location (if available)")
    @JsonProperty("currentLocation")
    LocationDto currentLocation,
    
    @Schema(description = "Creation timestamp", example = "2026-01-03T10:00:00")
    @JsonProperty("createdAt")
    LocalDateTime createdAt,
    
    @Schema(description = "Last update timestamp", example = "2026-01-03T10:30:00")
    @JsonProperty("updatedAt")
    LocalDateTime updatedAt
) {}

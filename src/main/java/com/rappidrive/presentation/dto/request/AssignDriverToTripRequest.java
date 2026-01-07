package com.rappidrive.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for assigning a driver to a trip.
 */
@Schema(description = "Request to assign a driver to a trip")
public record AssignDriverToTripRequest(
    
    @Schema(description = "Driver ID to assign", example = "123e4567-e89b-12d3-a456-426614174002", required = true)
    @NotNull(message = "Driver ID is required")
    @JsonProperty("driverId")
    UUID driverId
) {}

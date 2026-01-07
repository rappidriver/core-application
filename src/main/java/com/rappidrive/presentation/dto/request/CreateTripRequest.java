package com.rappidrive.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rappidrive.presentation.dto.common.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a new trip.
 */
@Schema(description = "Request to create a new trip")
public record CreateTripRequest(
    
    @Schema(description = "Tenant ID for multi-tenancy", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "Tenant ID is required")
    @JsonProperty("tenantId")
    UUID tenantId,
    
    @Schema(description = "Passenger ID requesting the trip", example = "123e4567-e89b-12d3-a456-426614174001", required = true)
    @NotNull(message = "Passenger ID is required")
    @JsonProperty("passengerId")
    UUID passengerId,
    
    @Schema(description = "Trip origin location", required = true)
    @NotNull(message = "Origin location is required")
    @Valid
    @JsonProperty("origin")
    LocationDto origin,
    
    @Schema(description = "Trip destination location", required = true)
    @NotNull(message = "Destination location is required")
    @Valid
    @JsonProperty("destination")
    LocationDto destination
) {}

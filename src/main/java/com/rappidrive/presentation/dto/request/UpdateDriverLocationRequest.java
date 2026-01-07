package com.rappidrive.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rappidrive.presentation.dto.common.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating driver's location.
 */
@Schema(description = "Request to update driver's current location")
public record UpdateDriverLocationRequest(
    
    @Schema(description = "Driver's new location", required = true)
    @NotNull(message = "Location is required")
    @Valid
    @JsonProperty("location")
    LocationDto location
) {}

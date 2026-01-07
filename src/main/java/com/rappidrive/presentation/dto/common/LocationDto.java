package com.rappidrive.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for geographic location.
 */
@Schema(description = "Geographic location with latitude and longitude")
public record LocationDto(
    
    @Schema(description = "Latitude coordinate", example = "-23.550520", required = true)
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @JsonProperty("latitude")
    Double latitude,
    
    @Schema(description = "Longitude coordinate", example = "-46.633308", required = true)
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @JsonProperty("longitude")
    Double longitude
) {}

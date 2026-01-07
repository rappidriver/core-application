package com.rappidrive.presentation.dto.request;

import com.rappidrive.domain.enums.VehicleType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for fare calculation request.
 */
public record CalculateFareRequest(
    
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,
    
    @NotNull(message = "Distance in km is required")
    @DecimalMin(value = "0.1", message = "Distance must be at least 0.1 km")
    BigDecimal distanceInKm,
    
    @NotNull(message = "Duration in minutes is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Long durationInMinutes,
    
    @NotNull(message = "Trip start time is required")
    LocalDateTime tripStartTime,
    
    @NotNull(message = "Vehicle type is required")
    VehicleType vehicleType
) {
}

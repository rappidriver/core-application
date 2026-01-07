package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for updating fare configuration.
 */
public record UpdateFareConfigurationRequest(
    
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,
    
    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "0.0", message = "Base fare must be non-negative")
    BigDecimal baseFare,
    
    @NotNull(message = "Price per km is required")
    @DecimalMin(value = "0.0", message = "Price per km must be non-negative")
    BigDecimal pricePerKm,
    
    @NotNull(message = "Price per minute is required")
    @DecimalMin(value = "0.0", message = "Price per minute must be non-negative")
    BigDecimal pricePerMinute,
    
    @NotNull(message = "Minimum fare is required")
    @DecimalMin(value = "0.0", message = "Minimum fare must be non-negative")
    BigDecimal minimumFare,
    
    @NotNull(message = "Platform commission rate is required")
    @DecimalMin(value = "0.0", message = "Commission rate must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Commission rate must be between 0 and 1")
    Double platformCommissionRate
) {
}

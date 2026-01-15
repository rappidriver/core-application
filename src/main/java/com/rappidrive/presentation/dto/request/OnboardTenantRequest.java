package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for tenant onboarding.
 * Validates all input before reaching the use case.
 */
public record OnboardTenantRequest(
        
        @NotBlank(message = "Tenant ID is required")
        String tenantId,
        
        @NotBlank(message = "Display name is required")
        String displayName,
        
        @NotBlank(message = "Admin email is required")
        @Email(message = "Admin email must be valid")
        String adminEmail,
        
        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., BRL, USD)")
        String currency,
        
        @NotNull(message = "Base fare is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base fare must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Base fare must have max 2 decimal places")
        BigDecimal baseFare,
        
        @NotNull(message = "Price per km is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price per km must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price per km must have max 2 decimal places")
        BigDecimal pricePerKm,
        
        @NotNull(message = "Price per minute is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price per minute must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price per minute must have max 2 decimal places")
        BigDecimal pricePerMin,
        
        @NotBlank(message = "Service area name is required")
        String serviceAreaName,
        
        @NotBlank(message = "GeoJSON polygon is required")
        String geoJsonPolygon
) {
}

package com.rappidrive.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for fare configuration response.
 */
public record FareConfigurationResponse(
    UUID id,
    UUID tenantId,
    BigDecimal baseFare,
    BigDecimal pricePerKm,
    BigDecimal pricePerMinute,
    BigDecimal minimumFare,
    Double platformCommissionRate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

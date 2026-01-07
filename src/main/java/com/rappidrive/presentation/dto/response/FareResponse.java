package com.rappidrive.presentation.dto.response;

import java.math.BigDecimal;

/**
 * DTO for fare calculation response.
 */
public record FareResponse(
    BigDecimal baseFare,
    BigDecimal distanceFare,
    BigDecimal timeFare,
    BigDecimal subtotal,
    BigDecimal timeMultiplier,
    BigDecimal vehicleMultiplier,
    BigDecimal finalAmount,
    String explanation
) {
}

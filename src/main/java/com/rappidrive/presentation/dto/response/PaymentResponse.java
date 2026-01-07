package com.rappidrive.presentation.dto.response;

import com.rappidrive.domain.enums.PaymentMethodType;
import com.rappidrive.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for payment response.
 */
public record PaymentResponse(
    UUID id,
    UUID tenantId,
    UUID tripId,
    BigDecimal amount,
    String currency,
    BigDecimal platformFee,
    BigDecimal driverAmount,
    PaymentMethodType paymentMethodType,
    String cardLast4,
    String cardBrand,
    String pixKey,
    PaymentStatus status,
    String gatewayTransactionId,
    String failureReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

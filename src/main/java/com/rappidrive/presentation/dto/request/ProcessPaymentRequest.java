package com.rappidrive.presentation.dto.request;

import com.rappidrive.domain.enums.PaymentMethodType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for payment processing request.
 */
public record ProcessPaymentRequest(
    
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,
    
    @NotNull(message = "Trip ID is required")
    UUID tripId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,
    
    @NotNull(message = "Payment method type is required")
    PaymentMethodType paymentMethodType,
    
    @Size(min = 4, max = 4, message = "Card last 4 digits must be exactly 4 digits")
    @Pattern(regexp = "\\d{4}", message = "Card last 4 must contain only digits")
    String cardLast4,
    
    @Size(max = 50, message = "Card brand must not exceed 50 characters")
    String cardBrand,
    
    @Size(max = 200, message = "PIX key must not exceed 200 characters")
    String pixKey
) {
}

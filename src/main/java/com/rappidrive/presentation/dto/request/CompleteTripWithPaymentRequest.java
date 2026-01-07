package com.rappidrive.presentation.dto.request;

import com.rappidrive.presentation.dto.common.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for completing a trip with payment processing.
 */
public record CompleteTripWithPaymentRequest(
    
    @Valid
    @NotNull(message = "Dropoff location is required")
    LocationDto dropoffLocation,
    
    @Valid
    @NotNull(message = "Payment method is required")
    PaymentMethodDto paymentMethod
) {
    public record PaymentMethodDto(
        @NotNull(message = "Payment method type is required")
        @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|PIX|CASH", message = "Invalid payment method type")
        String type,
        
        @Pattern(regexp = "\\d{4}", message = "Card last 4 digits must be 4 numbers")
        String cardLast4,
        
        String cardBrand,
        
        String pixKey
    ) {}
}

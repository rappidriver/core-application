package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * DTO for payment refund request.
 */
public record RefundPaymentRequest(
    
    @NotNull(message = "Payment ID is required")
    UUID paymentId,
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    String reason
) {
}

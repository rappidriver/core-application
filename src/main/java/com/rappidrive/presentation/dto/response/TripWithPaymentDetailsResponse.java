package com.rappidrive.presentation.dto.response;

import java.util.UUID;

/**
 * Response DTO containing complete trip details with fare and payment information.
 */
public record TripWithPaymentDetailsResponse(
    UUID tripId,
    TripResponse trip,
    FareResponse fare,
    PaymentResponse payment,
    boolean isFullyPaid
) {
    public boolean hasFare() {
        return fare != null;
    }
    
    public boolean hasPayment() {
        return payment != null;
    }
}

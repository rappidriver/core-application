package com.rappidrive.domain.exceptions;

import java.util.UUID;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends DomainException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public static PaymentNotFoundException withId(UUID paymentId) {
        return new PaymentNotFoundException("Payment not found with id: " + paymentId);
    }
    
    public static PaymentNotFoundException withTripId(UUID tripId) {
        return new PaymentNotFoundException("Payment not found for trip id: " + tripId);
    }
}

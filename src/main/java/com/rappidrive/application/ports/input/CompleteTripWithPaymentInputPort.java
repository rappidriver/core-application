package com.rappidrive.application.ports.input;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.PaymentMethod;

import java.util.UUID;

/**
 * Input port for completing a trip with automatic fare calculation and payment processing.
 */
public interface CompleteTripWithPaymentInputPort {
    
    /**
     * Executes trip completion with fare and payment.
     * 
     * @param command completion command
     * @return completion result with trip, fare, and payment details
     */
    TripCompletionResult execute(CompleteTripWithPaymentCommand command);
    
    /**
     * Command for completing a trip with payment.
     */
    record CompleteTripWithPaymentCommand(
        UUID tripId,
        Location dropoffLocation,
        PaymentMethod paymentMethod
    ) {
        public CompleteTripWithPaymentCommand {
            if (tripId == null) {
                throw new IllegalArgumentException("Trip ID cannot be null");
            }
            if (dropoffLocation == null) {
                throw new IllegalArgumentException("Dropoff location cannot be null");
            }
            if (paymentMethod == null) {
                throw new IllegalArgumentException("Payment method cannot be null");
            }
        }
    }
    
    /**
     * Result of trip completion with payment.
     */
    record TripCompletionResult(
        Trip trip,
        Fare fare,
        Payment payment,
        boolean paymentSuccessful,
        String failureReason
    ) {
        public TripCompletionResult {
            if (trip == null) {
                throw new IllegalArgumentException("Trip cannot be null");
            }
            if (fare == null) {
                throw new IllegalArgumentException("Fare cannot be null");
            }
            if (payment == null) {
                throw new IllegalArgumentException("Payment cannot be null");
            }
        }
        
        public boolean hasFailureReason() {
            return failureReason != null && !failureReason.isBlank();
        }
    }
}

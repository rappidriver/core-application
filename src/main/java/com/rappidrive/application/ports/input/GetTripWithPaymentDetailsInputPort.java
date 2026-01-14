package com.rappidrive.application.ports.input;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.entities.Trip;

import java.util.UUID;

public interface GetTripWithPaymentDetailsInputPort {
    
    TripWithPaymentDetails execute(UUID tripId);
    
    /**
     * Trip details with associated fare and payment information.
     */
    record TripWithPaymentDetails(
        Trip trip,
        Fare fare,
        Payment payment
    ) {
        public TripWithPaymentDetails {
            if (trip == null) {
                throw new IllegalArgumentException("Trip cannot be null");
            }
        }
        
        public boolean hasFare() {
            return fare != null;
        }
        
        public boolean hasPayment() {
            return payment != null;
        }
        
        public boolean isFullyPaid() {
            return hasPayment() && payment.getStatus().name().equals("COMPLETED");
        }
    }
}

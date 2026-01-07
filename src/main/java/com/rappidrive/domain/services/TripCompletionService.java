package com.rappidrive.domain.services;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.InvalidTripStateException;
import com.rappidrive.domain.valueobjects.Location;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain service for trip completion orchestration.
 * Validates business rules for completing a trip with fare calculation and payment.
 */
public class TripCompletionService {
    
    private static final double MINIMUM_DISTANCE_KM = 0.1;
    private static final int MINIMUM_DURATION_MINUTES = 1;
    
    /**
     * Validates that a trip can be completed.
     * 
     * @param trip trip to validate
     * @param dropoffLocation actual dropoff location
     * @throws InvalidTripStateException if trip cannot be completed
     * @throws IllegalArgumentException if dropoffLocation is null
     */
    public void validateCanComplete(Trip trip, Location dropoffLocation) {
        if (dropoffLocation == null) {
            throw new IllegalArgumentException("Dropoff location cannot be null");
        }
        
        if (!trip.getStatus().name().equals("IN_PROGRESS")) {
            throw new InvalidTripStateException(
                "Trip must be IN_PROGRESS to complete. Current status: " + trip.getStatus()
            );
        }
        
        if (trip.getDriverId().isEmpty()) {
            throw new InvalidTripStateException("Trip must have an assigned driver to complete");
        }
        
        if (trip.getStartedAt().isEmpty()) {
            throw new InvalidTripStateException("Trip must have been started to complete");
        }
    }
    
    /**
     * Calculates actual trip distance in kilometers.
     * 
     * @param pickupLocation where passenger was picked up
     * @param dropoffLocation where passenger was dropped off
     * @return distance in kilometers (minimum 0.1 km)
     */
    public double calculateActualDistance(Location pickupLocation, Location dropoffLocation) {
        double distance = pickupLocation.distanceTo(dropoffLocation);
        return Math.max(distance, MINIMUM_DISTANCE_KM);
    }
    
    /**
     * Calculates actual trip duration in minutes.
     * 
     * @param startedAt when trip started
     * @return duration in minutes (minimum 1 minute)
     */
    public int calculateActualDuration(LocalDateTime startedAt) {
        Duration duration = Duration.between(startedAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        return (int) Math.max(minutes, MINIMUM_DURATION_MINUTES);
    }
    
    /**
     * Validates fare and payment are consistent.
     * 
     * @param fare calculated fare
     * @param payment processed payment
     * @throws IllegalArgumentException if fare and payment amounts don't match
     */
    public void validateFareAndPayment(Fare fare, Payment payment) {
        if (!fare.getTotalAmount().equals(payment.getAmount())) {
            throw new IllegalArgumentException(
                String.format("Fare amount (%s) must match payment amount (%s)",
                    fare.getTotalAmount(), payment.getAmount())
            );
        }
    }
}

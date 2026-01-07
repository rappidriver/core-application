package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.entities.Trip;

import java.util.UUID;

/**
 * Input port for completing a trip.
 */
public interface CompleteTripInputPort {
    
    /**
     * Completes a trip (passenger arrived at destination).
     *
     * @param tripId the trip ID to complete
     * @return the updated trip
     */
    Trip execute(UUID tripId);
}

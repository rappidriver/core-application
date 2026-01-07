package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.entities.Trip;

import java.util.UUID;

/**
 * Input port for starting a trip.
 */
public interface StartTripInputPort {
    
    /**
     * Starts a trip (driver picked up passenger).
     *
     * @param tripId the trip ID to start
     * @return the updated trip
     */
    Trip execute(UUID tripId);
}

package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.entities.Trip;

import java.util.UUID;

/**
 * Input port for assigning a driver to a trip.
 */
public interface AssignDriverToTripInputPort {
    
    /**
     * Assigns a driver to an existing trip.
     *
     * @param command the command containing trip and driver IDs
     * @return the updated trip
     */
    Trip execute(AssignDriverCommand command);
    
    /**
     * Command record for assigning a driver to a trip.
     */
    record AssignDriverCommand(
        UUID tripId,
        UUID driverId
    ) {}
}

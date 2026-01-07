package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.UUID;

/**
 * Input port for creating a new trip.
 */
public interface CreateTripInputPort {
    
    /**
     * Creates a new trip request in the system.
     *
     * @param command the command containing trip creation data
     * @return the created trip
     */
    Trip execute(CreateTripCommand command);
    
    /**
     * Command record for creating a trip.
     */
    record CreateTripCommand(
        TenantId tenantId,
        UUID passengerId,
        Location pickupLocation,
        Location dropoffLocation
    ) {}
}

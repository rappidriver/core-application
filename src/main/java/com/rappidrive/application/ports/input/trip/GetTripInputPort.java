package com.rappidrive.application.ports.input.trip;

import com.rappidrive.domain.entities.Trip;

import java.util.UUID;

/**
 * Input port for retrieving a trip by ID.
 */
public interface GetTripInputPort {
    
    /**
     * Gets a trip by its unique identifier.
     *
     * @param id the trip ID
     * @return the trip
     * @throws com.rappidrive.domain.exceptions.TripNotFoundException if trip not found
     */
    Trip execute(UUID id);
}

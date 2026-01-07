package com.rappidrive.application.ports.input.passenger;

import com.rappidrive.domain.entities.Passenger;

import java.util.UUID;

/**
 * Input port for retrieving a passenger by ID.
 */
public interface GetPassengerInputPort {
    
    /**
     * Gets a passenger by its unique identifier.
     *
     * @param id the passenger ID
     * @return the passenger
     * @throws com.rappidrive.domain.exceptions.PassengerNotFoundException if passenger not found
     */
    Passenger execute(UUID id);
}

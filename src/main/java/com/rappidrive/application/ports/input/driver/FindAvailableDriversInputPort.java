package com.rappidrive.application.ports.input.driver;

import com.rappidrive.domain.entities.Driver;

import java.util.List;

/**
 * Input port (use case interface) for finding available drivers near a location.
 * Part of the driving (primary) adapter in hexagonal architecture.
 */
public interface FindAvailableDriversInputPort {
    
    /**
     * Finds available drivers within a radius of the pickup location.
     * Drivers must be ACTIVE, have valid location, and CNH válida.
     * Results are ordered by distance (nearest first).
     * 
     * @param command contains pickup location, radius, and tenant ID
     * @return list of available drivers, ordered by proximity
     */
    List<Driver> execute(FindAvailableDriversCommand command);
}

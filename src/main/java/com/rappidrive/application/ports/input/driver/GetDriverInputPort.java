package com.rappidrive.application.ports.input.driver;

import com.rappidrive.domain.entities.Driver;

import java.util.UUID;

/**
 * Input port for retrieving a driver by ID.
 */
public interface GetDriverInputPort {
    
    /**
     * Gets a driver by its unique identifier.
     *
     * @param id the driver ID
     * @return the driver
     * @throws com.rappidrive.domain.exceptions.DriverNotFoundException if driver not found
     */
    Driver execute(UUID id);
}

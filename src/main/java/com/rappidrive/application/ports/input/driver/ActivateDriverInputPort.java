package com.rappidrive.application.ports.input.driver;

import com.rappidrive.domain.entities.Driver;

import java.util.UUID;

/**
 * Input port for activating a driver.
 */
public interface ActivateDriverInputPort {
    
    /**
     * Activates a driver, making them available to receive trips.
     *
     * @param driverId the driver ID to activate
     * @return the activated driver
     */
    Driver execute(UUID driverId);
}

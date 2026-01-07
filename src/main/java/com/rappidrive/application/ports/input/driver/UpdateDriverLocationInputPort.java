package com.rappidrive.application.ports.input.driver;

import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.Location;

import java.util.UUID;

/**
 * Input port for updating driver location.
 */
public interface UpdateDriverLocationInputPort {
    
    /**
     * Updates the driver's current location.
     *
     * @param command the command containing driver ID and new location
     * @return the updated driver
     */
    Driver execute(UpdateLocationCommand command);
    
    /**
     * Command record for updating driver location.
     */
    record UpdateLocationCommand(
        UUID driverId,
        Location location
    ) {}
}

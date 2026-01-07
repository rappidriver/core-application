package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.ports.input.driver.FindAvailableDriversCommand;
import com.rappidrive.application.ports.input.driver.FindAvailableDriversInputPort;
import com.rappidrive.application.ports.output.DriverGeoQueryPort;
import com.rappidrive.domain.entities.Driver;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Use case for finding available drivers near a pickup location.
 * Uses geospatial queries to find drivers within a radius.
 */
public class FindAvailableDriversUseCase implements FindAvailableDriversInputPort {
    
    private final DriverGeoQueryPort driverGeoQueryPort;

    public FindAvailableDriversUseCase(DriverGeoQueryPort driverGeoQueryPort) {
        this.driverGeoQueryPort = driverGeoQueryPort;
    }
    
    @Override
    public List<Driver> execute(FindAvailableDriversCommand command) {
        // Query drivers within radius using geospatial search
        List<Driver> driversNearby = driverGeoQueryPort.findAvailableDriversNearby(
            command.pickupLocation(),
            command.radiusKm(),
            command.tenantId()
        );
        
        // Filter by availability (ACTIVE + has location + valid CNH)
        // The domain method isAvailableForRide() encapsulates all business rules
        return driversNearby.stream()
            .filter(Driver::isAvailableForRide)
            .toList();
    }
}

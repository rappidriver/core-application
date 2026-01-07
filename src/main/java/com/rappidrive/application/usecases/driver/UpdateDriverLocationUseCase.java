package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.ports.input.driver.UpdateDriverLocationInputPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Use case for updating driver location.
 */
public class UpdateDriverLocationUseCase implements UpdateDriverLocationInputPort {
    
    private final DriverRepositoryPort driverRepository;

    public UpdateDriverLocationUseCase(DriverRepositoryPort driverRepository) {
        this.driverRepository = driverRepository;
    }
    
    @Override
    public Driver execute(UpdateLocationCommand command) {
        Driver driver = driverRepository.findById(command.driverId())
            .orElseThrow(() -> new DriverNotFoundException(command.driverId()));
        
        driver.updateLocation(command.location());
        
        return driverRepository.save(driver);
    }
}

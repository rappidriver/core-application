package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.ports.input.driver.ActivateDriverInputPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public class ActivateDriverUseCase implements ActivateDriverInputPort {
    
    private final DriverRepositoryPort driverRepository;

    public ActivateDriverUseCase(DriverRepositoryPort driverRepository) {
        this.driverRepository = driverRepository;
    }
    
    @Override
    public Driver execute(UUID driverId) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new DriverNotFoundException(driverId));
        
        driver.activate();
        
        return driverRepository.save(driver);
    }
}

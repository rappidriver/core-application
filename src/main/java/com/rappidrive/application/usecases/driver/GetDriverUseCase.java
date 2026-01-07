package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.ports.input.driver.GetDriverInputPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for getting a driver by ID.
 */
public class GetDriverUseCase implements GetDriverInputPort {
    
    private final DriverRepositoryPort driverRepository;

    public GetDriverUseCase(DriverRepositoryPort driverRepository) {
        this.driverRepository = driverRepository;
    }
    
    @Override
    public Driver execute(UUID id) {
        return driverRepository.findById(id)
            .orElseThrow(() -> new DriverNotFoundException(id));
    }
}

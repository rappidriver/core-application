package com.rappidrive.application.usecases.vehicle;

import com.rappidrive.application.ports.input.vehicle.GetVehicleInputPort;
import com.rappidrive.application.ports.output.VehicleRepositoryPort;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.exceptions.VehicleNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para consulta de veículos.
 */
public class GetVehicleUseCase implements GetVehicleInputPort {
    
    private final VehicleRepositoryPort vehicleRepository;
    
    public GetVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    
    @Override
    public Vehicle execute(UUID id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> VehicleNotFoundException.withId(id));
    }
    
    @Override
    public List<Vehicle> findByDriver(UUID driverId) {
        return vehicleRepository.findByDriverId(driverId);
    }
    
    @Override
    public Optional<Vehicle> findActiveByDriver(UUID driverId) {
        return vehicleRepository.findActiveByDriverId(driverId);
    }
}

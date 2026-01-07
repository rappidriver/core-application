package com.rappidrive.application.usecases.vehicle;

import com.rappidrive.application.ports.input.vehicle.AssignVehicleToDriverInputPort;
import com.rappidrive.application.ports.output.VehicleRepositoryPort;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.exceptions.VehicleNotFoundException;

/**
 * Caso de uso para associar veículo a motorista.
 */
public class AssignVehicleToDriverUseCase implements AssignVehicleToDriverInputPort {
    
    private final VehicleRepositoryPort vehicleRepository;
    
    public AssignVehicleToDriverUseCase(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    
    @Override
    public Vehicle execute(AssignVehicleCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
            .orElseThrow(() -> VehicleNotFoundException.withId(command.vehicleId()));
        
        vehicle.assignToDriver(command.driverId());
        
        return vehicleRepository.save(vehicle);
    }
}

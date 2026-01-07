package com.rappidrive.application.usecases.vehicle;

import com.rappidrive.application.ports.input.vehicle.CreateVehicleInputPort;
import com.rappidrive.application.ports.output.VehicleRepositoryPort;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.exceptions.InvalidVehicleStateException;

import java.util.UUID;

/**
 * Caso de uso para criação de veículos.
 */
public class CreateVehicleUseCase implements CreateVehicleInputPort {
    
    private final VehicleRepositoryPort vehicleRepository;
    
    public CreateVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    
    @Override
    public Vehicle execute(CreateVehicleCommand command) {
        // Validar placa única no tenant
        if (vehicleRepository.existsByLicensePlate(command.licensePlate(), command.tenantId())) {
            throw InvalidVehicleStateException.duplicateLicensePlate(
                command.licensePlate().getValue()
            );
        }
        
        // Criar veículo (status INACTIVE, sem motorista)
        Vehicle vehicle = new Vehicle(
            UUID.randomUUID(),
            command.tenantId(),
            command.licensePlate(),
            command.brand(),
            command.model(),
            command.year(),
            command.color(),
            command.type(),
            command.numberOfDoors(),
            command.seats()
        );
        
        return vehicleRepository.save(vehicle);
    }
}

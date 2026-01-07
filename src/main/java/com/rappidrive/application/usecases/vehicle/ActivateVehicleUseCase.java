package com.rappidrive.application.usecases.vehicle;

import com.rappidrive.application.ports.input.vehicle.ActivateVehicleInputPort;
import com.rappidrive.application.ports.output.VehicleRepositoryPort;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.exceptions.VehicleNotFoundException;

/**
 * Caso de uso para ativação de veículos.
 * Garante que apenas 1 veículo esteja ativo por motorista.
 */
public class ActivateVehicleUseCase implements ActivateVehicleInputPort {
    
    private final VehicleRepositoryPort vehicleRepository;
    
    public ActivateVehicleUseCase(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    
    @Override
    public Vehicle execute(java.util.UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> VehicleNotFoundException.withId(vehicleId));
        
        // Se houver outro veículo ativo do mesmo motorista, desativar
        if (vehicle.getDriverId() != null) {
            vehicleRepository.findActiveByDriverId(vehicle.getDriverId())
                .ifPresent(activeVehicle -> {
                    if (!activeVehicle.getId().equals(vehicleId)) {
                        activeVehicle.deactivate();
                        vehicleRepository.save(activeVehicle);
                    }
                });
        }
        
        // Ativar o veículo (validações de negócio dentro do método activate())
        vehicle.activate();
        
        return vehicleRepository.save(vehicle);
    }
}

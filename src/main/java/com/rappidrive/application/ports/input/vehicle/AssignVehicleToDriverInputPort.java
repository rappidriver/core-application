package com.rappidrive.application.ports.input.vehicle;

import com.rappidrive.domain.entities.Vehicle;

import java.util.UUID;

/**
 * Port (interface) para associar veículo a motorista.
 */
public interface AssignVehicleToDriverInputPort {
    
    /**
     * Associa um veículo a um motorista.
     * 
     * @param command dados da associação
     * @return veículo atualizado
     */
    Vehicle execute(AssignVehicleCommand command);
    
    /**
     * Command record para associação veículo-motorista.
     */
    record AssignVehicleCommand(
        UUID vehicleId,
        UUID driverId
    ) {
        public AssignVehicleCommand {
            if (vehicleId == null) {
                throw new IllegalArgumentException("Vehicle ID cannot be null");
            }
            if (driverId == null) {
                throw new IllegalArgumentException("Driver ID cannot be null");
            }
        }
    }
}

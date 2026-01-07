package com.rappidrive.application.ports.input.vehicle;

import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.enums.VehicleStatus;

import java.util.UUID;

/**
 * Port (interface) para atualização de veículos.
 */
public interface UpdateVehicleInputPort {
    
    /**
     * Atualiza informações de um veículo.
     * 
     * @param command dados de atualização
     * @return veículo atualizado
     */
    Vehicle execute(UpdateVehicleCommand command);
    
    /**
     * Command record para atualização de veículo.
     * Apenas campos mutáveis podem ser atualizados.
     */
    record UpdateVehicleCommand(
        UUID vehicleId,
        String color,
        VehicleStatus status
    ) {
        public UpdateVehicleCommand {
            if (vehicleId == null) {
                throw new IllegalArgumentException("Vehicle ID cannot be null");
            }
            if (color != null && color.isBlank()) {
                throw new IllegalArgumentException("Color cannot be empty");
            }
        }
    }
}

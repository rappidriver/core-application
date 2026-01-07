package com.rappidrive.application.ports.input.vehicle;

import com.rappidrive.domain.entities.Vehicle;

import java.util.UUID;

/**
 * Port (interface) para ativação de veículos.
 * Ativa o veículo e desativa o anterior (se existir).
 */
public interface ActivateVehicleInputPort {
    
    /**
     * Ativa um veículo, garantindo que apenas 1 fique ativo por motorista.
     * Se o motorista já possui outro veículo ativo, ele será desativado automaticamente.
     * 
     * @param vehicleId ID do veículo a ativar
     * @return veículo ativado
     * @throws com.rappidrive.domain.exceptions.VehicleNotFoundException se veículo não encontrado
     * @throws com.rappidrive.domain.exceptions.InvalidVehicleStateException se regras de ativação falharem
     */
    Vehicle execute(UUID vehicleId);
}

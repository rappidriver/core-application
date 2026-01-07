package com.rappidrive.application.ports.input.vehicle;

import com.rappidrive.domain.entities.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) para consulta de veículos.
 */
public interface GetVehicleInputPort {
    
    /**
     * Busca um veículo por ID.
     * 
     * @param id ID do veículo
     * @return veículo encontrado
     * @throws com.rappidrive.domain.exceptions.VehicleNotFoundException se não encontrado
     */
    Vehicle execute(UUID id);
    
    /**
     * Busca todos os veículos de um motorista.
     * 
     * @param driverId ID do motorista
     * @return lista de veículos
     */
    List<Vehicle> findByDriver(UUID driverId);
    
    /**
     * Busca o veículo ativo de um motorista.
     * 
     * @param driverId ID do motorista
     * @return Optional contendo o veículo ativo
     */
    Optional<Vehicle> findActiveByDriver(UUID driverId);
}

package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) para operações de persistência de veículos.
 * Implementado pela camada de infraestrutura (JPA adapter).
 */
public interface VehicleRepositoryPort {
    
    /**
     * Salva ou atualiza um veículo.
     * 
     * @param vehicle veículo a ser salvo
     * @return veículo salvo
     */
    Vehicle save(Vehicle vehicle);
    
    /**
     * Busca um veículo por ID.
     * 
     * @param id ID do veículo
     * @return Optional contendo o veículo se encontrado
     */
    Optional<Vehicle> findById(UUID id);
    
    /**
     * Busca todos os veículos de um tenant.
     * 
     * @param tenantId ID do tenant
     * @return lista de veículos
     */
    List<Vehicle> findByTenantId(TenantId tenantId);
    
    /**
     * Busca todos os veículos de um motorista.
     * 
     * @param driverId ID do motorista
     * @return lista de veículos
     */
    List<Vehicle> findByDriverId(UUID driverId);
    
    /**
     * Busca o veículo ativo de um motorista.
     * 
     * @param driverId ID do motorista
     * @return Optional contendo o veículo ativo se existir
     */
    Optional<Vehicle> findActiveByDriverId(UUID driverId);
    
    /**
     * Busca um veículo por placa dentro de um tenant.
     * 
     * @param licensePlate placa do veículo
     * @param tenantId ID do tenant
     * @return Optional contendo o veículo se encontrado
     */
    Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate, TenantId tenantId);
    
    /**
     * Verifica se existe um veículo com a placa especificada no tenant.
     * 
     * @param licensePlate placa do veículo
     * @param tenantId ID do tenant
     * @return true se existe
     */
    boolean existsByLicensePlate(LicensePlate licensePlate, TenantId tenantId);
    
    /**
     * Remove um veículo.
     * 
     * @param id ID do veículo
     */
    void delete(UUID id);
}

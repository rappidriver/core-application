package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for fare configuration persistence operations.
 */
public interface FareConfigurationRepositoryPort {
    
    /**
     * Saves a fare configuration.
     *
     * @param fareConfiguration the fare configuration to save
     * @return the saved fare configuration
     */
    FareConfiguration save(FareConfiguration fareConfiguration);
    
    /**
     * Finds a fare configuration by its ID.
     *
     * @param id the fare configuration ID
     * @return an Optional containing the fare configuration if found
     */
    Optional<FareConfiguration> findById(UUID id);
    
    /**
     * Finds a fare configuration by tenant ID.
     *
     * @param tenantId the tenant ID
     * @return an Optional containing the fare configuration if found
     */
    Optional<FareConfiguration> findByTenantId(TenantId tenantId);
    
    /**
     * Checks if a fare configuration exists for a tenant.
     *
     * @param tenantId the tenant ID
     * @return true if a configuration exists
     */
    boolean existsByTenantId(TenantId tenantId);
    
    /**
     * Deletes a fare configuration.
     *
     * @param fareConfiguration the fare configuration to delete
     */
    void delete(FareConfiguration fareConfiguration);
}

package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.FareConfigurationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for FareConfigurationJpaEntity.
 */
@Repository
public interface SpringDataFareConfigurationRepository extends JpaRepository<FareConfigurationJpaEntity, UUID> {
    
    /**
     * Finds fare configuration by tenant.
     * Each tenant should have only one fare configuration.
     */
    Optional<FareConfigurationJpaEntity> findByTenantId(UUID tenantId);
    
    /**
     * Checks if fare configuration exists for tenant.
     */
    boolean existsByTenantId(UUID tenantId);
}

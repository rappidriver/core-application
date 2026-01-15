package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.ServiceArea;
import com.rappidrive.domain.valueobjects.ServiceAreaId;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port for ServiceArea repository operations.
 * Follows Hexagonal Architecture - domain logic depends on this interface,
 * infrastructure layer provides the implementation.
 */
public interface ServiceAreaRepositoryPort {
    
    /**
     * Save a service area (create or update).
     */
    ServiceArea save(ServiceArea serviceArea);
    
    /**
     * Find a service area by its ID.
     */
    Optional<ServiceArea> findById(ServiceAreaId id);
    
    /**
     * Find all service areas for a specific tenant.
     */
    List<ServiceArea> findByTenantId(TenantId tenantId);
    
    /**
     * Find active service areas for a specific tenant.
     */
    List<ServiceArea> findActiveByTenantId(TenantId tenantId);
    
    /**
     * Check if a service area exists for a tenant.
     */
    boolean existsByTenantId(TenantId tenantId);
    
    /**
     * Delete a service area.
     */
    void delete(ServiceAreaId id);
}

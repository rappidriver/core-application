package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.infrastructure.persistence.entities.ServiceAreaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ServiceAreaJpaEntity.
 */
@Repository
public interface SpringDataServiceAreaRepository extends JpaRepository<ServiceAreaJpaEntity, UUID> {
    
    /**
     * Find all service areas for a specific tenant.
     */
    List<ServiceAreaJpaEntity> findByTenantId(UUID tenantId);
    
    /**
     * Find active service areas for a specific tenant.
     */
    @Query("SELECT s FROM ServiceAreaJpaEntity s WHERE s.tenantId = :tenantId AND s.active = true")
    List<ServiceAreaJpaEntity> findActiveByTenantId(@Param("tenantId") UUID tenantId);
    
    /**
     * Check if any service area exists for a tenant.
     */
    boolean existsByTenantId(UUID tenantId);
}

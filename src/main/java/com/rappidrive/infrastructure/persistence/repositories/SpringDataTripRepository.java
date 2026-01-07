package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.TripJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for TripJpaEntity.
 */
@Repository
public interface SpringDataTripRepository extends JpaRepository<TripJpaEntity, UUID> {
    
    List<TripJpaEntity> findByTenantId(TenantId tenantId);
    
    List<TripJpaEntity> findByDriverId(UUID driverId);
    
    List<TripJpaEntity> findByPassengerId(UUID passengerId);
    
    List<TripJpaEntity> findByStatusAndTenantId(TripStatus status, TenantId tenantId);
    
    @Query("SELECT t FROM TripJpaEntity t WHERE t.tenantId = :tenantId AND t.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')")
    List<TripJpaEntity> findActiveTrips(@Param("tenantId") TenantId tenantId);
}

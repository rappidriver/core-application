package com.rappidrive.infrastructure.persistence.repositories;

import com.rappidrive.domain.enums.DriverStatus;
import com.rappidrive.domain.valueobjects.CPF;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.DriverJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for DriverJpaEntity.
 * Includes PostGIS geospatial queries.
 */
@Repository
public interface SpringDataDriverRepository extends JpaRepository<DriverJpaEntity, UUID> {
    
    Optional<DriverJpaEntity> findByEmail(Email email);
    
    Optional<DriverJpaEntity> findByCpf(CPF cpf);
    
    List<DriverJpaEntity> findByTenantId(TenantId tenantId);
    
    List<DriverJpaEntity> findByStatusAndTenantId(DriverStatus status, TenantId tenantId);
    
    boolean existsByEmail(Email email);
    
    boolean existsByCpf(CPF cpf);
    
    /**
     * Finds drivers within a radius using PostGIS ST_DWithin.
     * ST_DWithin uses spatial index (GIST) for performance.
     * Results are ordered by distance (nearest first).
     * 
     * @param latitude pickup latitude
     * @param longitude pickup longitude
     * @param radiusMeters search radius in meters
     * @param tenantId tenant identifier for multi-tenancy
     * @return list of drivers within radius, ordered by distance
     */
    @Query(value = """
        SELECT d.*
        FROM drivers d
        WHERE d.tenant_id = :tenantId
          AND d.location_latitude IS NOT NULL
          AND d.location_longitude IS NOT NULL
          AND ST_DWithin(
              ST_SetSRID(ST_MakePoint(d.location_longitude, d.location_latitude), 4326)::geography,
              ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
              :radiusMeters
          )
        ORDER BY ST_Distance(
          ST_SetSRID(ST_MakePoint(d.location_longitude, d.location_latitude), 4326)::geography,
          ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        )
        """, nativeQuery = true)
    List<DriverJpaEntity> findDriversWithinRadius(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeters") double radiusMeters,
        @Param("tenantId") TenantId tenantId
    );
}

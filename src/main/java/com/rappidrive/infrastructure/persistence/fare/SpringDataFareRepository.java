package com.rappidrive.infrastructure.persistence.fare;

import com.rappidrive.infrastructure.persistence.entities.FareJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Fare entity.
 */
@Repository
public interface SpringDataFareRepository extends JpaRepository<FareJpaEntity, UUID> {
    
    /**
     * Finds a fare by trip ID.
     *
     * @param tripId trip ID
     * @return fare if found
     */
    Optional<FareJpaEntity> findByTripId(UUID tripId);
    
    /**
     * Finds all fares for a tenant within a time period.
     *
     * @param tenantId tenant ID
     * @param startDate period start
     * @param endDate period end
     * @return list of fares
     */
    List<FareJpaEntity> findByTenantIdAndCreatedAtBetween(
        UUID tenantId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    /**
     * Checks if a fare exists for a trip.
     *
     * @param tripId trip ID
     * @return true if fare exists
     */
    boolean existsByTripId(UUID tripId);
}

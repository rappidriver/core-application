package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Fare persistence operations.
 */
public interface FareRepositoryPort {
    
    /**
     * Saves a fare.
     * 
     * @param fare fare to save
     * @return saved fare
     */
    Fare save(Fare fare);
    
    /**
     * Finds a fare by its unique identifier.
     * 
     * @param id fare ID
     * @return fare if found
     */
    Optional<Fare> findById(UUID id);
    
    /**
     * Finds a fare by trip ID.
     * 
     * @param tripId trip ID
     * @return fare if found
     */
    Optional<Fare> findByTripId(UUID tripId);
    
    /**
     * Finds all fares for a tenant within a time period.
     * 
     * @param tenantId tenant ID
     * @param startDate period start
     * @param endDate period end
     * @return list of fares
     */
    List<Fare> findByTenantIdAndCreatedAtBetween(TenantId tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Checks if a fare exists for a trip.
     * 
     * @param tripId trip ID
     * @return true if fare exists
     */
    boolean existsByTripId(UUID tripId);
}

package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.enums.TripStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Trip repository operations.
 * This interface defines the contract for persisting and retrieving Trip entities.
 * Implementations must be provided in the infrastructure layer.
 */
public interface TripRepositoryPort {
    
    /**
     * Saves a trip entity.
     *
     * @param trip the trip to save
     * @return the saved trip with updated metadata (e.g., generated ID if new)
     */
    Trip save(Trip trip);
    
    /**
     * Finds a trip by its unique identifier.
     *
     * @param id the trip ID
     * @return an Optional containing the trip if found, empty otherwise
     */
    Optional<Trip> findById(UUID id);
    
    /**
     * Finds all trips belonging to a specific tenant.
     *
     * @param tenantId the tenant identifier
     * @return list of trips for the tenant
     */
    List<Trip> findByTenantId(TenantId tenantId);
    
    /**
     * Finds all trips for a specific driver.
     *
     * @param driverId the driver's unique identifier
     * @return list of trips assigned to the driver
     */
    List<Trip> findByDriver(UUID driverId);
    
    /**
     * Finds all trips for a specific passenger.
     *
     * @param passengerId the passenger's unique identifier
     * @return list of trips requested by the passenger
     */
    List<Trip> findByPassenger(UUID passengerId);
    
    /**
     * Finds all trips with a specific status for a given tenant.
     *
     * @param status the trip status to filter by
     * @param tenantId the tenant identifier
     * @return list of trips matching the status
     */
    List<Trip> findByStatus(TripStatus status, TenantId tenantId);
    
    /**
     * Finds all active trips (PENDING, ACCEPTED, IN_PROGRESS) for a tenant.
     *
     * @param tenantId the tenant identifier
     * @return list of active trips
     */
    List<Trip> findActiveTrips(TenantId tenantId);
    
    /**
     * Deletes a trip by ID.
     *
     * @param id the trip ID to delete
     */
    void delete(UUID id);
}

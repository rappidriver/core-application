package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.List;

/**
 * Output port for geospatial queries on drivers.
 * Implements driven (secondary) adapter for location-based driver searches.
 */
public interface DriverGeoQueryPort {
    
    /**
     * Finds available drivers near a specific location.
     * Uses PostGIS spatial queries to search within a radius.
     * Results are ordered by distance (nearest first).
     * 
     * @param pickupLocation the pickup location coordinates
     * @param radiusKm search radius in kilometers
     * @param tenantId tenant identifier for multi-tenancy isolation
     * @return list of drivers within radius, ordered by distance
     */
    List<Driver> findAvailableDriversNearby(Location pickupLocation, double radiusKm, TenantId tenantId);
}

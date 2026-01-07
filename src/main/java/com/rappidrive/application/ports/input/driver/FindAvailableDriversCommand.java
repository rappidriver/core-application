package com.rappidrive.application.ports.input.driver;

import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.TenantId;

/**
 * Command for finding available drivers near a pickup location.
 * 
 * @param tenantId tenant identifier for multi-tenancy
 * @param pickupLocation the pickup location coordinates
 * @param radiusKm search radius in kilometers (default: 5km)
 */
public record FindAvailableDriversCommand(
    TenantId tenantId,
    Location pickupLocation,
    double radiusKm
) {
    public FindAvailableDriversCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        if (pickupLocation == null) {
            throw new IllegalArgumentException("Pickup location cannot be null");
        }
        if (radiusKm <= 0 || radiusKm > 50) {
            throw new IllegalArgumentException("Radius must be between 0 and 50 km");
        }
    }
    
    /**
     * Creates command with default 5km radius.
     */
    public FindAvailableDriversCommand(TenantId tenantId, Location pickupLocation) {
        this(tenantId, pickupLocation, 5.0);
    }
}

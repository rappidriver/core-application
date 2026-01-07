package com.rappidrive.domain.enums;

/**
 * Status of a trip in its lifecycle.
 */
public enum TripStatus {
    /**
     * Trip requested by passenger, waiting for driver assignment.
     */
    REQUESTED,
    
    /**
     * Driver assigned to trip, waiting for driver to start.
     */
    DRIVER_ASSIGNED,
    
    /**
     * Trip in progress (driver picked up passenger).
     */
    IN_PROGRESS,
    
    /**
     * Trip completed successfully.
     */
    COMPLETED,
    
    /**
     * Trip cancelled by passenger, driver, or system.
     */
    CANCELLED
}

package com.rappidrive.domain.enums;

/**
 * Status of a driver in the system.
 */
public enum DriverStatus {
    /**
     * Driver registered but pending approval/verification.
     */
    PENDING_APPROVAL,
    
    /**
     * Driver approved and active, can accept rides.
     */
    ACTIVE,
    
    /**
     * Driver busy on a trip, cannot accept new rides.
     */
    BUSY,
    
    /**
     * Driver deactivated (by driver or system), cannot accept rides.
     */
    INACTIVE,
    
    /**
     * Driver blocked due to violations, cannot access system.
     */
    BLOCKED
}

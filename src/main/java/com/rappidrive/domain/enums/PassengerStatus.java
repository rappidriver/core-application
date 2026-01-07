package com.rappidrive.domain.enums;

/**
 * Status of a passenger in the system.
 */
public enum PassengerStatus {
    /**
     * Passenger active, can request rides.
     */
    ACTIVE,
    
    /**
     * Passenger deactivated (by passenger or system), cannot request rides.
     */
    INACTIVE,
    
    /**
     * Passenger blocked due to violations, cannot access system.
     */
    BLOCKED
}

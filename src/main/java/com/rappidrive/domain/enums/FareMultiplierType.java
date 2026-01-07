package com.rappidrive.domain.enums;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Time-based fare multiplier for dynamic pricing.
 */
public enum FareMultiplierType {
    /**
     * Normal hours - standard pricing.
     */
    NORMAL(1.0),
    
    /**
     * Peak hours (7-9 AM, 5-7 PM) - increased pricing.
     */
    PEAK(1.5),
    
    /**
     * Late night (12 AM - 6 AM) - increased pricing for safety and availability.
     */
    LATE_NIGHT(1.3);
    
    private final double multiplier;
    
    FareMultiplierType(double multiplier) {
        this.multiplier = multiplier;
    }
    
    /**
     * Gets the multiplier value for this fare type.
     *
     * @return the multiplier value (e.g., 1.0 for normal, 1.5 for peak)
     */
    public double getMultiplier() {
        return multiplier;
    }
    
    /**
     * Determines the appropriate fare multiplier based on the trip time.
     *
     * @param tripTime the time when the trip occurred
     * @return the appropriate FareMultiplierType
     */
    public static FareMultiplierType fromTripTime(LocalDateTime tripTime) {
        LocalTime time = tripTime.toLocalTime();
        
        // Late night: 00:00 - 06:00
        if (time.isBefore(LocalTime.of(6, 0))) {
            return LATE_NIGHT;
        }
        
        // Morning peak: 07:00 - 09:00
        if (time.isAfter(LocalTime.of(6, 59)) && time.isBefore(LocalTime.of(9, 0))) {
            return PEAK;
        }
        
        // Evening peak: 17:00 - 19:00
        if (time.isAfter(LocalTime.of(16, 59)) && time.isBefore(LocalTime.of(19, 0))) {
            return PEAK;
        }
        
        // Normal hours
        return NORMAL;
    }
}

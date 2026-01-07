package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a driver identifier.
 * Prevents mixing up driver ID with other UUID fields.
 */
public final class DriverId {
    
    private final UUID value;
    
    public DriverId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("DriverId cannot be null");
        }
        this.value = value;
    }
    
    public static DriverId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DriverId string cannot be null or empty");
        }
        
        try {
            return new DriverId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid DriverId UUID format: " + value, e);
        }
    }
    
    public static DriverId generate() {
        return new DriverId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    public String asString() {
        return value.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverId that = (DriverId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
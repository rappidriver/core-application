package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a trip identifier.
 * Prevents mixing up trip ID with other UUID fields.
 */
public final class TripId {
    
    private final UUID value;
    
    public TripId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("TripId cannot be null");
        }
        this.value = value;
    }
    
    public static TripId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TripId string cannot be null or empty");
        }
        
        try {
            return new TripId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TripId UUID format: " + value, e);
        }
    }
    
    public static TripId generate() {
        return new TripId(UUID.randomUUID());
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
        TripId that = (TripId) o;
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
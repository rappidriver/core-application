package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a user identifier.
 * Immutable and based on UUID.
 */
public final class UserId {
    
    private final UUID value;
    
    /**
     * Creates a new UserId from a UUID.
     * 
     * @param value the UUID value
     * @throws IllegalArgumentException if value is null
     */
    public UserId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Creates a new UserId from a string UUID.
     * 
     * @param value the UUID string
     * @return a new UserId instance
     * @throws IllegalArgumentException if value is null, empty, or invalid UUID format
     */
    public static UserId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId string cannot be null or empty");
        }
        
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value, e);
        }
    }
    
    /**
     * Generates a new random UserId.
     * 
     * @return a new UserId with a random UUID
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    
    /**
     * Returns the UUID value.
     * 
     * @return the UUID
     */
    public UUID getValue() {
        return value;
    }
    
    /**
     * Returns the string representation of the UUID.
     * 
     * @return the UUID as a string
     */
    public String asString() {
        return value.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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

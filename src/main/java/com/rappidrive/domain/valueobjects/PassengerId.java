package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a passenger identifier.
 * Prevents mixing up passenger ID with other UUID fields.
 */
public final class PassengerId {
    
    private final UUID value;
    
    public PassengerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PassengerId cannot be null");
        }
        this.value = value;
    }
    
    public static PassengerId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PassengerId string cannot be null or empty");
        }
        
        try {
            return new PassengerId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid PassengerId UUID format: " + value, e);
        }
    }
    
    public static PassengerId generate() {
        return new PassengerId(UUID.randomUUID());
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
        PassengerId that = (PassengerId) o;
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
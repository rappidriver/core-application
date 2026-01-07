package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a tenant identifier in a multi-tenant system.
 * Immutable and based on UUID.
 */
public final class TenantId {
    
    private final UUID value;
    
    /**
     * Creates a new TenantId from a UUID.
     * 
     * @param value the UUID value
     * @throws IllegalArgumentException if value is null
     */
    public TenantId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Creates a new TenantId from a string UUID.
     * 
     * @param value the UUID string
     * @return a new TenantId instance
     * @throws IllegalArgumentException if value is null, empty, or invalid UUID format
     */
    public static TenantId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TenantId string cannot be null or empty");
        }
        
        try {
            return new TenantId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value, e);
        }
    }
    
    /**
     * Generates a new random TenantId.
     * 
     * @return a new TenantId with a random UUID
     */
    public static TenantId generate() {
        return new TenantId(UUID.randomUUID());
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
        TenantId tenantId = (TenantId) o;
        return Objects.equals(value, tenantId.value);
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

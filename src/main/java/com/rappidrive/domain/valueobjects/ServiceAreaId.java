package com.rappidrive.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique identifier for a Service Area.
 * Immutable by design.
 */
public final class ServiceAreaId {
    
    private final UUID value;

    private ServiceAreaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ServiceAreaId cannot be null");
        }
        this.value = value;
    }

    public static ServiceAreaId of(UUID value) {
        return new ServiceAreaId(value);
    }

    public static ServiceAreaId of(String value) {
        try {
            return new ServiceAreaId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ServiceAreaId format: " + value);
        }
    }

    public static ServiceAreaId generate() {
        return new ServiceAreaId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceAreaId that = (ServiceAreaId) o;
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

package com.rappidrive.domain.valueobjects;

import java.util.Objects;

/**
 * Represents a geographic search zone for parallel driver queries.
 * Used to divide large search areas into smaller zones that can be queried in parallel.
 * 
 * <p>This value object is immutable and ensures coordinates are valid.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * Location center = new Location(-23.550520, -46.633308);
 * SearchZone zone = new SearchZone(center, 5.0);
 * }</pre>
 */
public record SearchZone(Location center, double radiusKm) {
    
    /**
     * Creates a search zone with validation.
     * 
     * @param center Geographic center of the search zone
     * @param radiusKm Search radius in kilometers
     * @throws IllegalArgumentException if center is null or radius is invalid
     */
    public SearchZone {
        Objects.requireNonNull(center, "Search zone center cannot be null");
        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Search zone radius must be positive, got: " + radiusKm);
        }
        if (radiusKm > 100) {
            throw new IllegalArgumentException("Search zone radius cannot exceed 100km, got: " + radiusKm);
        }
    }
    
    /**
     * Checks if a location is within this search zone.
     * 
     * @param location Location to check
     * @return true if location is within the radius
     */
    public boolean contains(Location location) {
        return center.distanceTo(location) <= radiusKm;
    }
    
    /**
     * Returns a human-readable description of this zone.
     * 
     * @return Description string
     */
    public String describe() {
        return String.format("SearchZone[center=(%s), radius=%.1fkm]", 
            center.toString(), radiusKm);
    }
}

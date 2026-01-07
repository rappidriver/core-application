package com.rappidrive.domain.valueobjects;

import java.util.Locale;
import java.util.Objects;

/**
 * Value object representing a geographic location with latitude and longitude.
 * Immutable and validates coordinate ranges.
 * Provides distance calculation using Haversine formula.
 */
public final class Location {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    
    private final double latitude;
    private final double longitude;
    
    /**
     * Creates a new Location instance.
     * 
     * @param latitude the latitude in degrees (-90 to +90)
     * @param longitude the longitude in degrees (-180 to +180)
     * @throws IllegalArgumentException if coordinates are out of valid range
     */
    public Location(double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    private void validateLatitude(double latitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException(
                String.format("Latitude must be between %s and %s, got: %s",
                    MIN_LATITUDE, MAX_LATITUDE, latitude)
            );
        }
    }
    
    private void validateLongitude(double longitude) {
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                String.format("Longitude must be between %s and %s, got: %s",
                    MIN_LONGITUDE, MAX_LONGITUDE, longitude)
            );
        }
    }
    
    /**
     * Calculates the distance to another location using the Haversine formula.
     * 
     * @param other the other location
     * @return distance in kilometers
     * @throws IllegalArgumentException if other is null
     */
    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Other location cannot be null");
        }
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.latitude, latitude) == 0 &&
               Double.compare(location.longitude, longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
    
    @Override
    public String toString() {
        return String.format(Locale.US, "Location(lat=%f, lon=%f)", latitude, longitude);
    }
}

package com.rappidrive.application.ports.output;

import com.rappidrive.domain.valueobjects.Location;

/**
 * Output port for distance calculation using geospatial functions.
 */
public interface DistanceCalculationPort {
    
    /**
     * Calculates the geodesic distance between two locations.
     * Uses PostGIS ST_Distance_Sphere for accurate spherical distance calculation.
     * 
     * @param from starting location
     * @param to ending location
     * @return distance in kilometers
     */
    double calculateDistance(Location from, Location to);
}

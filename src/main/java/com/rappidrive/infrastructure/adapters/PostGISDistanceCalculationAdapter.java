package com.rappidrive.infrastructure.adapters;

import com.rappidrive.application.ports.output.DistanceCalculationPort;
import com.rappidrive.domain.valueobjects.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter for calculating distances using PostGIS.
 * Uses ST_Distance_Sphere for accurate geodesic distance calculation.
 */
@Component
public class PostGISDistanceCalculationAdapter implements DistanceCalculationPort {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PostGISDistanceCalculationAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public double calculateDistance(Location from, Location to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }
        
        // Use PostGIS ST_Distance_Sphere to calculate geodesic distance in meters
        // Then convert to kilometers
        String sql = """
            SELECT ST_Distance_Sphere(
                ST_MakePoint(?, ?),
                ST_MakePoint(?, ?)
            ) / 1000.0 as distance_km
            """;
        
        Double distanceKm = jdbcTemplate.queryForObject(
            sql,
            Double.class,
            from.getLongitude(),
            from.getLatitude(),
            to.getLongitude(),
            to.getLatitude()
        );
        
        return distanceKm != null ? Math.round(distanceKm * 100.0) / 100.0 : 0.0;
    }
}

package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.concurrency.ParallelExecutor;
import com.rappidrive.application.ports.input.driver.FindAvailableDriversCommand;
import com.rappidrive.application.ports.input.driver.FindAvailableDriversInputPort;
import com.rappidrive.application.ports.output.DriverGeoQueryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.SearchZone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Use case for finding available drivers near a pickup location.
 * Uses parallel geospatial queries across multiple zones for improved performance.
 */
public class FindAvailableDriversUseCase implements FindAvailableDriversInputPort {
    
    private final DriverGeoQueryPort driverGeoQueryPort;
    private final ExecutorService virtualThreadExecutor;

    public FindAvailableDriversUseCase(DriverGeoQueryPort driverGeoQueryPort,
                                       ExecutorService virtualThreadExecutor) {
        this.driverGeoQueryPort = driverGeoQueryPort;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }
    
    @Override
    public List<Driver> execute(FindAvailableDriversCommand command) {
        Location pickupLocation = command.pickupLocation();
        double radiusKm = command.radiusKm();
        
        // Divide search area into zones for parallel querying
        List<SearchZone> searchZones = divideIntoSearchZones(pickupLocation, radiusKm);
        
        try {
            // Execute parallel searches across all zones using CompletableFuture
            List<List<Driver>> zoneResults = ParallelExecutor.mapParallel(
                searchZones,
                zone -> driverGeoQueryPort.findAvailableDriversNearby(
                    zone.center(),
                    zone.radiusKm(),
                    command.tenantId()
                ),
                virtualThreadExecutor
            );
            
            // Flatten results and filter by availability
            return zoneResults.stream()
                .flatMap(List::stream)
                .distinct() // Remove duplicates (drivers may appear in multiple zones)
                .filter(Driver::isAvailableForRide)
                .limit(10) // Return top 10 nearest drivers
                .toList();
                
        } catch (Exception e) {
            throw new DriverSearchException("Failed to search for drivers in parallel", e);
        }
    }
    
    /**
     * Divides a circular search area into 4 quadrant zones for parallel searching.
     * This allows database queries to run concurrently, reducing total search time.
     * 
     * @param center Center point of search
     * @param radiusKm Total search radius
     * @return List of 4 search zones (NE, SE, SW, NW quadrants)
     */
    private List<SearchZone> divideIntoSearchZones(Location center, double radiusKm) {
        // Calculate offset for zone centers (roughly 45 degrees from center)
        double offset = radiusKm * 0.5; // Half radius creates overlapping zones
        
        List<SearchZone> zones = new ArrayList<>(4);
        
        // Northeast quadrant
        zones.add(new SearchZone(
            new Location(center.getLatitude() + offset * 0.009, center.getLongitude() + offset * 0.009),
            radiusKm * 0.6
        ));
        
        // Southeast quadrant
        zones.add(new SearchZone(
            new Location(center.getLatitude() - offset * 0.009, center.getLongitude() + offset * 0.009),
            radiusKm * 0.6
        ));
        
        // Southwest quadrant
        zones.add(new SearchZone(
            new Location(center.getLatitude() - offset * 0.009, center.getLongitude() - offset * 0.009),
            radiusKm * 0.6
        ));
        
        // Northwest quadrant
        zones.add(new SearchZone(
            new Location(center.getLatitude() + offset * 0.009, center.getLongitude() - offset * 0.009),
            radiusKm * 0.6
        ));
        
        return zones;
    }
}

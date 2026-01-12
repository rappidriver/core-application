package com.rappidrive.application.usecases.driver;

import com.rappidrive.application.concurrency.ParallelExecutor;
import com.rappidrive.application.metrics.DriverAssignmentAttemptStatus;
import com.rappidrive.application.metrics.DriverAssignmentStage;
import com.rappidrive.application.ports.input.driver.FindAvailableDriversCommand;
import com.rappidrive.application.ports.input.driver.FindAvailableDriversInputPort;
import com.rappidrive.application.ports.output.DriverAssignmentMetricsPort;
import com.rappidrive.application.ports.output.DriverGeoQueryPort;
import com.rappidrive.application.ports.output.TelemetryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.SearchZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Use case for finding available drivers near a pickup location.
 * Uses parallel geospatial queries across multiple zones for improved performance.
 */
public class FindAvailableDriversUseCase implements FindAvailableDriversInputPort {
    
    private final DriverGeoQueryPort driverGeoQueryPort;
    private final ExecutorService virtualThreadExecutor;
    private final TelemetryPort telemetryPort;
    private final DriverAssignmentMetricsPort metricsPort;

    public FindAvailableDriversUseCase(DriverGeoQueryPort driverGeoQueryPort,
                                       ExecutorService virtualThreadExecutor,
                                       TelemetryPort telemetryPort,
                                       DriverAssignmentMetricsPort metricsPort) {
        this.driverGeoQueryPort = driverGeoQueryPort;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.telemetryPort = telemetryPort;
        this.metricsPort = metricsPort;
    }
    
    @Override
    public List<Driver> execute(FindAvailableDriversCommand command) {
        Map<String, String> attributes = telemetryAttributes(command);
        return telemetryPort.traceUseCase("driver.search", attributes, () -> executeWithMetrics(command));
    }

    private List<Driver> executeWithMetrics(FindAvailableDriversCommand command) {
        long startTime = System.nanoTime();
        try {
            List<Driver> drivers = performDriverSearch(command);
            metricsPort.incrementAttempts(DriverAssignmentStage.SEARCH, DriverAssignmentAttemptStatus.SUCCESS);
            return drivers;
        } catch (RuntimeException ex) {
            metricsPort.incrementAttempts(DriverAssignmentStage.SEARCH, DriverAssignmentAttemptStatus.ERROR);
            throw ex;
        } finally {
            metricsPort.recordStageDuration(DriverAssignmentStage.SEARCH, elapsedMillis(startTime));
        }
    }

    private List<Driver> performDriverSearch(FindAvailableDriversCommand command) {
        Location pickupLocation = command.pickupLocation();
        double radiusKm = command.radiusKm();
        List<SearchZone> searchZones = divideIntoSearchZones(pickupLocation, radiusKm);

        try {
            List<List<Driver>> zoneResults = ParallelExecutor.mapParallel(
                searchZones,
                zone -> driverGeoQueryPort.findAvailableDriversNearby(
                    zone.center(),
                    zone.radiusKm(),
                    command.tenantId()
                ),
                virtualThreadExecutor
            );

            return zoneResults.stream()
                .flatMap(List::stream)
                .distinct()
                .filter(Driver::isAvailableForRide)
                .limit(10)
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

    private Map<String, String> telemetryAttributes(FindAvailableDriversCommand command) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("stage", "driver.search");
        attributes.put("tenantId", command.tenantId().asString());
        attributes.put("tripId", command.tripId() != null ? command.tripId().toString() : "unknown");
        return attributes;
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}

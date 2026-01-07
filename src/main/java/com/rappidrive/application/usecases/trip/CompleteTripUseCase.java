package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.trip.CompleteTripInputPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.exceptions.TripNotFoundException;
import com.rappidrive.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for completing a trip.
 */
public class CompleteTripUseCase implements CompleteTripInputPort {
    
    private final TripRepositoryPort tripRepository;
    private final DriverRepositoryPort driverRepository;

    public CompleteTripUseCase(TripRepositoryPort tripRepository, DriverRepositoryPort driverRepository) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
    }
    
    @Override
    public Trip execute(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException(tripId));
        
        // Complete trip with actual fare (using estimated for now)
        Money actualFare = trip.getEstimatedFare();
        trip.complete(actualFare);
        
        // Free up the driver
        if (trip.getDriverId().isPresent()) {
            UUID driverId = trip.getDriverId().get();
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId));
            driver.activate(); // Back to ACTIVE status
            driverRepository.save(driver);
        }
        
        return tripRepository.save(trip);
    }
}

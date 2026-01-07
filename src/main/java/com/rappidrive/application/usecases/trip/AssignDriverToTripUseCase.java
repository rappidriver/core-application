package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.trip.AssignDriverToTripInputPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.exceptions.TripNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Use case for assigning a driver to a trip.
 */
public class AssignDriverToTripUseCase implements AssignDriverToTripInputPort {
    
    private final TripRepositoryPort tripRepository;
    private final DriverRepositoryPort driverRepository;
    private final com.rappidrive.domain.events.DomainEventPublisher eventPublisher;

    public AssignDriverToTripUseCase(TripRepositoryPort tripRepository, DriverRepositoryPort driverRepository, com.rappidrive.domain.events.DomainEventPublisher eventPublisher) {
        this.tripRepository = tripRepository;
        this.driverRepository = driverRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Trip execute(AssignDriverCommand command) {
        Trip trip = tripRepository.findById(command.tripId())
            .orElseThrow(() -> new TripNotFoundException(command.tripId()));
        
        // Ensure trip is still pending
        if (!trip.isPending()) {
            throw new IllegalStateException("Trip is not pending");
        }

        Driver driver = driverRepository.findById(command.driverId())
            .orElseThrow(() -> new DriverNotFoundException(command.driverId()));
        
        // Validate driver is available
        if (!driver.isAvailableForRide()) {
            throw new IllegalStateException("Driver is not available for rides");
        }
        
        // Assign driver to trip (domain action)
        trip.assignDriver(new com.rappidrive.domain.valueobjects.DriverId(command.driverId()));

        // Update driver status to BUSY and persist
        driver.markAsBusy();
        driverRepository.save(driver);

        // Persist trip and handle optimistic locking conflicts
        try {
            Trip saved = tripRepository.save(trip);
            // Publish domain event
            eventPublisher.publish(new com.rappidrive.domain.events.TripDriverAssignedEvent(saved.getId(), new com.rappidrive.domain.valueobjects.DriverId(command.driverId())));
            return saved;
        } catch (com.rappidrive.domain.exceptions.TripConcurrencyException e) {
            throw new com.rappidrive.application.exceptions.TripAlreadyAcceptedException("Esta corrida já foi aceita por outro motorista", e);
        }
    }
}

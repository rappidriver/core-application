package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.trip.CreateTripInputPort;
import com.rappidrive.application.ports.output.PassengerRepositoryPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Passenger;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.events.TripCreatedEvent;
import com.rappidrive.domain.exceptions.PassengerNotFoundException;
import com.rappidrive.domain.services.FareCalculator;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.PassengerId;
import com.rappidrive.domain.valueobjects.TripId;
import com.rappidrive.domain.services.StandardFareCalculator;
import lombok.RequiredArgsConstructor;

/**
 * Use case for creating a new trip.
 */
public class CreateTripUseCase implements CreateTripInputPort {

    private final TripRepositoryPort tripRepository;
    private final PassengerRepositoryPort passengerRepository;
    private final FareCalculator fareCalculator;
    private final DomainEventPublisher eventPublisher;

    public CreateTripUseCase(TripRepositoryPort tripRepository, PassengerRepositoryPort passengerRepository) {
        this.tripRepository = tripRepository;
        this.passengerRepository = passengerRepository;
        this.fareCalculator = new StandardFareCalculator();
        this.eventPublisher = DomainEventPublisher.instance();
    }

    @Override
    public Trip execute(CreateTripCommand command) {
        // Validate passenger exists and can request rides
        Passenger passenger = passengerRepository.findById(command.passengerId())
            .orElseThrow(() -> new PassengerNotFoundException(command.passengerId()));

        if (!passenger.canRequestRide()) {
            throw new IllegalStateException("Passenger cannot request rides in current status: " + passenger.getStatus());
        }

        // Calculate estimated fare
        Money estimatedFare = fareCalculator.calculateFare(
            new FareCalculator.FareCalculationRequest(
                command.pickupLocation(),
                command.dropoffLocation(),
                command.pickupLocation().distanceTo(command.dropoffLocation()),
                false, // Assume non-peak hour for simplicity
                1.0 // No surge pricing
            )
        );

        // Create trip
        Trip trip = new Trip(
            TripId.generate(),
            command.tenantId(),
            new PassengerId(command.passengerId()),
            command.pickupLocation(),
            command.dropoffLocation(),
            estimatedFare
        );

        // Save trip
        trip = tripRepository.save(trip);

        // Publish event
        eventPublisher.publish(new TripCreatedEvent(
            trip.getId(),
            trip.getPassengerId(),
            String.format("%s,%s", trip.getOrigin().getLatitude(), trip.getOrigin().getLongitude()),
            String.format("%s,%s", trip.getDestination().getLatitude(), trip.getDestination().getLongitude()),
            trip.getEstimatedDistanceKm(),
            trip.getEstimatedFare().toString()
        ));

        return trip;
    }
}

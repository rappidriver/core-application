package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.trip.StartTripInputPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.TripNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for starting a trip.
 */
public class StartTripUseCase implements StartTripInputPort {
    
    private final TripRepositoryPort tripRepository;

    public StartTripUseCase(TripRepositoryPort tripRepository) {
        this.tripRepository = tripRepository;
    }
    
    @Override
    public Trip execute(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException(tripId));
        
        trip.start();
        
        return tripRepository.save(trip);
    }
}

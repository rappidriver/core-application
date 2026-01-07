package com.rappidrive.application.usecases.trip;

import com.rappidrive.application.ports.input.trip.GetTripInputPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.exceptions.TripNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for getting a trip by ID.
 */
public class GetTripUseCase implements GetTripInputPort {
    
    private final TripRepositoryPort tripRepository;

    public GetTripUseCase(TripRepositoryPort tripRepository) {
        this.tripRepository = tripRepository;
    }
    
    @Override
    public Trip execute(UUID id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> new TripNotFoundException(id));
    }
}

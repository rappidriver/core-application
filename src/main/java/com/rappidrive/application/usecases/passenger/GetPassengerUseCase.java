package com.rappidrive.application.usecases.passenger;

import com.rappidrive.application.ports.input.passenger.GetPassengerInputPort;
import com.rappidrive.application.ports.output.PassengerRepositoryPort;
import com.rappidrive.domain.entities.Passenger;
import com.rappidrive.domain.exceptions.PassengerNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case for getting a passenger by ID.
 */
public class GetPassengerUseCase implements GetPassengerInputPort {
    
    private final PassengerRepositoryPort passengerRepository;

    public GetPassengerUseCase(PassengerRepositoryPort passengerRepository) {
        this.passengerRepository = passengerRepository;
    }
    
    @Override
    public Passenger execute(UUID id) {
        return passengerRepository.findById(id)
            .orElseThrow(() -> new PassengerNotFoundException(id));
    }
}

package com.rappidrive.application.usecases.rating;

import com.rappidrive.application.ports.input.rating.GetTripRatingsInputPort;
import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingType;

import java.util.List;
import java.util.UUID;

/**
 * Use case para obter avaliações de uma viagem.
 */
public class GetTripRatingsUseCase implements GetTripRatingsInputPort {
    
    private final RatingRepositoryPort ratingRepository;
    
    public GetTripRatingsUseCase(RatingRepositoryPort ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
    
    @Override
    public TripRatingsInfo execute(UUID tripId) {
        // Buscar todas as avaliações da viagem
        List<Rating> ratings = ratingRepository.findByTripId(tripId);
        
        // Separar por tipo
        Rating passengerRating = ratings.stream()
            .filter(Rating::isPassengerRating)
            .findFirst()
            .orElse(null);
        
        Rating driverRating = ratings.stream()
            .filter(Rating::isDriverRating)
            .findFirst()
            .orElse(null);
        
        return new TripRatingsInfo(tripId, passengerRating, driverRating);
    }
}

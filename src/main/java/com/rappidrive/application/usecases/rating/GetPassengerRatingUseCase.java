package com.rappidrive.application.usecases.rating;

import com.rappidrive.application.ports.input.rating.GetPassengerRatingInputPort;
import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;

import java.util.UUID;

public class GetPassengerRatingUseCase implements GetPassengerRatingInputPort {
    
    private final RatingRepositoryPort ratingRepository;
    
    public GetPassengerRatingUseCase(RatingRepositoryPort ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
    
    @Override
    public PassengerRatingInfo execute(UUID passengerId) {
        // Calcular média de ratings ativos
        double averageRating = ratingRepository.calculateAverageByRateeId(
            passengerId,
            RatingType.PASSENGER_BY_DRIVER,
            RatingStatus.ACTIVE
        );
        
        // Contar total de ratings ativos
        long totalRatings = ratingRepository.countByRateeIdAndType(
            passengerId,
            RatingType.PASSENGER_BY_DRIVER,
            RatingStatus.ACTIVE
        );
        
        return new PassengerRatingInfo(
            passengerId,
            averageRating,
            totalRatings
        );
    }
}

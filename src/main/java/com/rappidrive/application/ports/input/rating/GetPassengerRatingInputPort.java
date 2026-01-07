package com.rappidrive.application.ports.input.rating;

import java.util.UUID;

/**
 * Input port para obter rating de passageiro.
 */
public interface GetPassengerRatingInputPort {
    
    /**
     * Informações de rating de um passageiro.
     */
    record PassengerRatingInfo(
        UUID passengerId,
        double averageRating,
        long totalRatings
    ) {
        public boolean hasRatings() {
            return totalRatings > 0;
        }
    }
    
    /**
     * Executa busca de rating do passageiro.
     */
    PassengerRatingInfo execute(UUID passengerId);
}

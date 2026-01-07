package com.rappidrive.application.ports.input.rating;

import com.rappidrive.domain.entities.Rating;

import java.util.UUID;

/**
 * Input port para obter avaliações de uma viagem.
 */
public interface GetTripRatingsInputPort {
    
    /**
     * Avaliações de uma viagem (bidirecional).
     */
    record TripRatingsInfo(
        UUID tripId,
        Rating passengerRating,
        Rating driverRating
    ) {
        public boolean hasPassengerRating() {
            return passengerRating != null;
        }
        
        public boolean hasDriverRating() {
            return driverRating != null;
        }
        
        public boolean hasBothRatings() {
            return hasPassengerRating() && hasDriverRating();
        }
    }
    
    /**
     * Executa busca de avaliações da viagem.
     */
    TripRatingsInfo execute(UUID tripId);
}

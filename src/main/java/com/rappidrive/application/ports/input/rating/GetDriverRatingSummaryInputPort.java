package com.rappidrive.application.ports.input.rating;

import com.rappidrive.domain.entities.Rating;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Input port para obter resumo de avaliações de motorista.
 */
public interface GetDriverRatingSummaryInputPort {
    
    /**
     * Resumo completo de avaliações de um motorista.
     */
    record DriverRatingSummary(
        UUID driverId,
        double averageRating,
        long totalRatings,
        Map<Integer, Long> ratingDistribution,
        List<Rating> recentRatings
    ) {
        public boolean hasRatings() {
            return totalRatings > 0;
        }
        
        public boolean hasExcellentRating() {
            return averageRating >= 4.5;
        }
    }
    
    /**
     * Executa busca de resumo de avaliações do motorista.
     */
    DriverRatingSummary execute(UUID driverId);
}

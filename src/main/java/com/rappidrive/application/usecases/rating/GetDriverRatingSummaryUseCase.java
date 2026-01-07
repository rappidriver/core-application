package com.rappidrive.application.usecases.rating;

import com.rappidrive.application.ports.input.rating.GetDriverRatingSummaryInputPort;
import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case para obter resumo de avaliações de motorista.
 */
public class GetDriverRatingSummaryUseCase implements GetDriverRatingSummaryInputPort {
    
    private static final int RECENT_RATINGS_LIMIT = 10;
    
    private final RatingRepositoryPort ratingRepository;
    
    public GetDriverRatingSummaryUseCase(RatingRepositoryPort ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
    
    @Override
    public DriverRatingSummary execute(UUID driverId) {
        // 1. Calcular média de ratings ativos
        double averageRating = ratingRepository.calculateAverageByRateeId(
            driverId,
            RatingType.DRIVER_BY_PASSENGER,
            RatingStatus.ACTIVE
        );
        
        // 2. Contar total de ratings ativos
        long totalRatings = ratingRepository.countByRateeIdAndType(
            driverId,
            RatingType.DRIVER_BY_PASSENGER,
            RatingStatus.ACTIVE
        );
        
        // 3. Buscar todas as avaliações ativas para calcular distribuição
        List<Rating> allRatings = ratingRepository.findByRateeIdAndType(
            driverId,
            RatingType.DRIVER_BY_PASSENGER,
            RatingStatus.ACTIVE
        );
        
        Map<Integer, Long> distribution = calculateDistribution(allRatings);
        
        // 4. Buscar últimas 10 avaliações
        List<Rating> recentRatings = ratingRepository.findRecentByRateeId(
            driverId,
            RatingType.DRIVER_BY_PASSENGER,
            RECENT_RATINGS_LIMIT
        );
        
        return new DriverRatingSummary(
            driverId,
            averageRating,
            totalRatings,
            distribution,
            recentRatings
        );
    }
    
    /**
     * Calcula distribuição de ratings (quantos 1★, 2★, etc).
     */
    private Map<Integer, Long> calculateDistribution(List<Rating> ratings) {
        Map<Integer, Long> distribution = ratings.stream()
            .collect(Collectors.groupingBy(
                r -> r.getScore().value(),
                Collectors.counting()
            ));
        
        // Garantir que todos os scores 1-5 existam no mapa
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }
        
        return distribution;
    }
}

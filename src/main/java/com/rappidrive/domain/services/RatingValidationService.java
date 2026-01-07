package com.rappidrive.domain.services;

import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.domain.enums.TripStatus;
import com.rappidrive.domain.exceptions.DuplicateRatingException;
import com.rappidrive.domain.exceptions.RatingDeadlineExpiredException;
import com.rappidrive.domain.exceptions.TripNotCompletedException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Domain Service para validações relacionadas a avaliações.
 */
public class RatingValidationService {
    
    private static final long RATING_DEADLINE_DAYS = 7;
    
    /**
     * Valida se uma viagem pode ser avaliada.
     * 
     * @param trip Viagem a ser validada
     * @throws TripNotCompletedException se viagem não está completa
     * @throws RatingDeadlineExpiredException se prazo de 7 dias expirou
     */
    public void validateCanRate(Trip trip) {
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new TripNotCompletedException(
                String.format("Viagem %s não está completa. Status atual: %s",
                    trip.getId(), trip.getStatus())
            );
        }
        
        if (!trip.getCompletedAt().isPresent()) {
            throw new TripNotCompletedException(
                String.format("Viagem %s marcada como completa mas sem completedAt", trip.getId())
            );
        }
        
        if (isRatingDeadlineExpired(trip.getCompletedAt().get())) {
            throw new RatingDeadlineExpiredException(
                String.format("Prazo de %d dias para avaliar viagem %s expirou",
                    RATING_DEADLINE_DAYS, trip.getId())
            );
        }
    }
    
    /**
     * Valida se já existe uma avaliação do mesmo tipo para a viagem.
     * 
     * @param tripId ID da viagem
     * @param raterId ID de quem avalia
     * @param type Tipo de avaliação
     * @param alreadyExists se já existe avaliação
     * @throws DuplicateRatingException se já existe
     */
    public void validateNoDuplicateRating(
            UUID tripId,
            UUID raterId,
            RatingType type,
            boolean alreadyExists
    ) {
        if (alreadyExists) {
            throw new DuplicateRatingException(
                String.format("Já existe avaliação do tipo %s para viagem %s pelo usuário %s",
                    type, tripId, raterId)
            );
        }
    }
    
    /**
     * Verifica se o prazo para avaliar expirou.
     */
    private boolean isRatingDeadlineExpired(LocalDateTime completedAt) {
        long daysSinceCompletion = ChronoUnit.DAYS.between(completedAt, LocalDateTime.now());
        return daysSinceCompletion > RATING_DEADLINE_DAYS;
    }
    
    /**
     * Verifica se ainda está dentro do prazo para avaliar.
     */
    public boolean isWithinRatingDeadline(LocalDateTime completedAt) {
        return !isRatingDeadlineExpired(completedAt);
    }
    
    /**
     * Retorna quantos dias faltam para o prazo expirar.
     */
    public long getDaysUntilDeadline(LocalDateTime completedAt) {
        long daysSinceCompletion = ChronoUnit.DAYS.between(completedAt, LocalDateTime.now());
        long daysRemaining = RATING_DEADLINE_DAYS - daysSinceCompletion;
        return Math.max(0, daysRemaining);
    }
}

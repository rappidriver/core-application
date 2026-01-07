package com.rappidrive.application.ports.input.rating;

import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingType;

import java.util.UUID;

/**
 * Input port para criar avaliação.
 */
public interface CreateRatingInputPort {
    
    /**
     * Comando para criar avaliação.
     */
    record CreateRatingCommand(
        UUID tripId,
        UUID raterId,
        UUID rateeId,
        RatingType type,
        int score,
        String comment
    ) {}
    
    /**
     * Executa criação de avaliação.
     */
    Rating execute(CreateRatingCommand command);
}

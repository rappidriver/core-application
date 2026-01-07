package com.rappidrive.application.ports.input.rating;

import java.util.UUID;

/**
 * Input port para reportar avaliação ofensiva.
 */
public interface ReportOffensiveRatingInputPort {
    
    /**
     * Comando para reportar avaliação.
     */
    record ReportRatingCommand(
        UUID ratingId,
        UUID reporterId,
        String reason
    ) {}
    
    /**
     * Executa report de avaliação ofensiva.
     */
    void execute(ReportRatingCommand command);
}

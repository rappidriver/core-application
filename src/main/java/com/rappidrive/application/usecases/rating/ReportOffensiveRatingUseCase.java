package com.rappidrive.application.usecases.rating;

import com.rappidrive.application.ports.input.rating.ReportOffensiveRatingInputPort;
import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.exceptions.RatingNotFoundException;

/**
 * Use case para reportar avaliação ofensiva.
 */
public class ReportOffensiveRatingUseCase implements ReportOffensiveRatingInputPort {
    
    private final RatingRepositoryPort ratingRepository;
    
    public ReportOffensiveRatingUseCase(RatingRepositoryPort ratingRepository) {
        this.ratingRepository = ratingRepository;
    }
    
    @Override
    public void execute(ReportRatingCommand command) {
        Rating rating = ratingRepository.findById(command.ratingId())
            .orElseThrow(() -> new RatingNotFoundException(
                String.format("Avaliação %s não encontrada", command.ratingId())
            ));
        
        rating.markAsReported();
        
        ratingRepository.save(rating);
        
        // TODO: Notificar admin sobre report
        // TODO: Log para auditoria
    }
}

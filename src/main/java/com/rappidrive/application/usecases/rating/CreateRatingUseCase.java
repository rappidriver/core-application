package com.rappidrive.application.usecases.rating;

import com.rappidrive.application.ports.input.rating.CreateRatingInputPort;
import com.rappidrive.application.ports.output.RatingRepositoryPort;
import com.rappidrive.application.ports.output.TripRepositoryPort;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.domain.exceptions.TripNotFoundException;
import com.rappidrive.domain.services.RatingValidationService;
import com.rappidrive.domain.valueobjects.RatingComment;
import com.rappidrive.domain.valueobjects.RatingScore;

/**
 * Use case para criar avaliação.
 */
public class CreateRatingUseCase implements CreateRatingInputPort {
    
    private final RatingRepositoryPort ratingRepository;
    private final TripRepositoryPort tripRepository;
    private final RatingValidationService validationService;
    
    public CreateRatingUseCase(
            RatingRepositoryPort ratingRepository,
            TripRepositoryPort tripRepository,
            RatingValidationService validationService
    ) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
        this.validationService = validationService;
    }
    
    @Override
    public Rating execute(CreateRatingCommand command) {
        // 1. Buscar viagem
        Trip trip = tripRepository.findById(command.tripId())
            .orElseThrow(() -> new TripNotFoundException(
                String.format("Viagem %s não encontrada", command.tripId())
            ));
        
        // 2. Validar se viagem pode ser avaliada (COMPLETED, dentro do prazo)
        validationService.validateCanRate(trip);
        
        // 3. Validar se não existe avaliação duplicada
        boolean alreadyExists = ratingRepository.existsByTripIdAndRaterIdAndType(
            command.tripId(),
            command.raterId(),
            command.type()
        );
        validationService.validateNoDuplicateRating(
            command.tripId(),
            command.raterId(),
            command.type(),
            alreadyExists
        );
        
        // 4. Criar rating
        RatingScore score = RatingScore.of(command.score());
        RatingComment comment = RatingComment.of(command.comment());
        
        Rating rating = new Rating(
            command.tripId(),
            command.raterId(),
            command.rateeId(),
            command.type(),
            score,
            comment,
            trip.getTenantId()
        );
        
        // 5. Salvar
        return ratingRepository.save(rating);
    }
}

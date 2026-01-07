package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.rating.*;
import com.rappidrive.application.ports.input.rating.CreateRatingInputPort.CreateRatingCommand;
import com.rappidrive.application.ports.input.rating.GetDriverRatingSummaryInputPort.DriverRatingSummary;
import com.rappidrive.application.ports.input.rating.GetPassengerRatingInputPort.PassengerRatingInfo;
import com.rappidrive.application.ports.input.rating.GetTripRatingsInputPort.TripRatingsInfo;
import com.rappidrive.application.ports.input.rating.ReportOffensiveRatingInputPort.ReportRatingCommand;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.presentation.dto.request.CreateRatingRequest;
import com.rappidrive.presentation.dto.request.ReportRatingRequest;
import com.rappidrive.presentation.dto.response.*;
import com.rappidrive.presentation.mappers.RatingDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller para endpoints de avaliações.
 */
@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {
    
    private final CreateRatingInputPort createRatingUseCase;
    private final GetDriverRatingSummaryInputPort getDriverRatingSummaryUseCase;
    private final GetPassengerRatingInputPort getPassengerRatingUseCase;
    private final GetTripRatingsInputPort getTripRatingsUseCase;
    private final ReportOffensiveRatingInputPort reportOffensiveRatingUseCase;
    private final RatingDtoMapper mapper;
    
    public RatingController(
            CreateRatingInputPort createRatingUseCase,
            GetDriverRatingSummaryInputPort getDriverRatingSummaryUseCase,
            GetPassengerRatingInputPort getPassengerRatingUseCase,
            GetTripRatingsInputPort getTripRatingsUseCase,
            ReportOffensiveRatingInputPort reportOffensiveRatingUseCase,
            RatingDtoMapper mapper
    ) {
        this.createRatingUseCase = createRatingUseCase;
        this.getDriverRatingSummaryUseCase = getDriverRatingSummaryUseCase;
        this.getPassengerRatingUseCase = getPassengerRatingUseCase;
        this.getTripRatingsUseCase = getTripRatingsUseCase;
        this.reportOffensiveRatingUseCase = reportOffensiveRatingUseCase;
        this.mapper = mapper;
    }
    
    /**
     * POST /api/v1/ratings
     * Criar avaliação (passageiro ou motorista).
     * 
     * @param request Dados da avaliação
     * @param raterId ID de quem avalia (X-User-Id header)
     * @param rateeId ID de quem é avaliado (query param)
     * @return RatingResponse criado
     */
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(
            @Valid @RequestBody CreateRatingRequest request,
            @RequestHeader("X-User-Id") UUID raterId,
            @RequestParam UUID rateeId
    ) {
        CreateRatingCommand command = mapper.toCommand(request, raterId, rateeId);
        Rating rating = createRatingUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toRatingResponse(rating));
    }
    
    /**
     * GET /api/v1/ratings/drivers/{driverId}/summary
     * Obter resumo de avaliações do motorista.
     * 
     * @param driverId ID do motorista
     * @return Resumo com média, distribuição e últimas avaliações
     */
    @GetMapping("/drivers/{driverId}/summary")
    public ResponseEntity<DriverRatingSummaryResponse> getDriverRatingSummary(
            @PathVariable UUID driverId
    ) {
        DriverRatingSummary summary = getDriverRatingSummaryUseCase.execute(driverId);
        return ResponseEntity.ok(mapper.toDriverRatingSummaryResponse(summary));
    }
    
    /**
     * GET /api/v1/ratings/passengers/{passengerId}
     * Obter rating do passageiro.
     * 
     * @param passengerId ID do passageiro
     * @return Informações de rating (média e total)
     */
    @GetMapping("/passengers/{passengerId}")
    public ResponseEntity<PassengerRatingInfoResponse> getPassengerRating(
            @PathVariable UUID passengerId
    ) {
        PassengerRatingInfo info = getPassengerRatingUseCase.execute(passengerId);
        return ResponseEntity.ok(mapper.toPassengerRatingInfoResponse(info));
    }
    
    /**
     * GET /api/v1/ratings/trips/{tripId}
     * Obter avaliações da viagem (ambas direções).
     * 
     * @param tripId ID da viagem
     * @return Avaliações do passageiro e do motorista
     */
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<TripRatingsResponse> getTripRatings(
            @PathVariable UUID tripId
    ) {
        TripRatingsInfo info = getTripRatingsUseCase.execute(tripId);
        return ResponseEntity.ok(mapper.toTripRatingsResponse(info));
    }
    
    /**
     * POST /api/v1/ratings/{ratingId}/report
     * Reportar avaliação ofensiva.
     * 
     * @param ratingId ID da avaliação
     * @param request Motivo do report
     * @param reporterId ID de quem reporta (X-User-Id header)
     * @return 204 No Content
     */
    @PostMapping("/{ratingId}/report")
    public ResponseEntity<Void> reportRating(
            @PathVariable UUID ratingId,
            @Valid @RequestBody ReportRatingRequest request,
            @RequestHeader("X-User-Id") UUID reporterId
    ) {
        ReportRatingCommand command = new ReportRatingCommand(
            ratingId,
            reporterId,
            request.reason()
        );
        reportOffensiveRatingUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
}

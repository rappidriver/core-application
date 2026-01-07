package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.rating.CreateRatingInputPort.CreateRatingCommand;
import com.rappidrive.application.ports.input.rating.GetDriverRatingSummaryInputPort.DriverRatingSummary;
import com.rappidrive.application.ports.input.rating.GetPassengerRatingInputPort.PassengerRatingInfo;
import com.rappidrive.application.ports.input.rating.GetTripRatingsInputPort.TripRatingsInfo;
import com.rappidrive.domain.entities.Rating;
import com.rappidrive.domain.enums.RatingStatus;
import com.rappidrive.domain.enums.RatingType;
import com.rappidrive.presentation.dto.request.CreateRatingRequest;
import com.rappidrive.presentation.dto.request.CreateRatingRequest.RatingTypeDto;
import com.rappidrive.presentation.dto.response.*;
import com.rappidrive.presentation.dto.response.RatingResponse.RatingStatusDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre DTOs e domain/commands de Rating.
 */
@Component
public class RatingDtoMapper {
    
    /**
     * Converte request para comando de criação.
     */
    public CreateRatingCommand toCommand(CreateRatingRequest request, UUID raterId, UUID rateeId) {
        return new CreateRatingCommand(
            request.tripId(),
            raterId,
            rateeId,
            toRatingType(request.type()),
            request.score(),
            request.comment()
        );
    }
    
    /**
     * Converte Rating entity para RatingResponse DTO.
     */
    public RatingResponse toRatingResponse(Rating rating) {
        if (rating == null) {
            return null;
        }
        
        return new RatingResponse(
            rating.getId(),
            rating.getTripId(),
            rating.getRaterId(),
            rating.getRateeId(),
            toRatingResponseTypeDto(rating.getType()),
            rating.getScore().value(),
            rating.getComment().value(),
            toRatingStatusDto(rating.getStatus()),
            rating.getCreatedAt()
        );
    }
    
    /**
     * Converte lista de ratings.
     */
    public List<RatingResponse> toRatingResponseList(List<Rating> ratings) {
        return ratings.stream()
            .map(this::toRatingResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Converte DriverRatingSummary para DriverRatingSummaryResponse.
     */
    public DriverRatingSummaryResponse toDriverRatingSummaryResponse(DriverRatingSummary summary) {
        return new DriverRatingSummaryResponse(
            summary.driverId(),
            summary.averageRating(),
            summary.totalRatings(),
            summary.ratingDistribution(),
            toRatingResponseList(summary.recentRatings())
        );
    }
    
    /**
     * Converte PassengerRatingInfo para PassengerRatingInfoResponse.
     */
    public PassengerRatingInfoResponse toPassengerRatingInfoResponse(PassengerRatingInfo info) {
        return new PassengerRatingInfoResponse(
            info.passengerId(),
            info.averageRating(),
            info.totalRatings()
        );
    }
    
    /**
     * Converte TripRatingsInfo para TripRatingsResponse.
     */
    public TripRatingsResponse toTripRatingsResponse(TripRatingsInfo info) {
        return new TripRatingsResponse(
            info.tripId(),
            toRatingResponse(info.passengerRating()),
            toRatingResponse(info.driverRating()),
            !info.hasPassengerRating(),
            !info.hasDriverRating()
        );
    }
    
    // Enum conversions
    
    private RatingType toRatingType(RatingTypeDto dto) {
        return switch (dto) {
            case DRIVER_BY_PASSENGER -> RatingType.DRIVER_BY_PASSENGER;
            case PASSENGER_BY_DRIVER -> RatingType.PASSENGER_BY_DRIVER;
        };
    }
    
    private RatingResponse.RatingTypeDto toRatingResponseTypeDto(RatingType type) {
        return switch (type) {
            case DRIVER_BY_PASSENGER -> RatingResponse.RatingTypeDto.DRIVER_BY_PASSENGER;
            case PASSENGER_BY_DRIVER -> RatingResponse.RatingTypeDto.PASSENGER_BY_DRIVER;
        };
    }
    
    private RatingStatusDto toRatingStatusDto(RatingStatus status) {
        return switch (status) {
            case ACTIVE -> RatingStatusDto.ACTIVE;
            case REPORTED -> RatingStatusDto.REPORTED;
            case DELETED -> RatingStatusDto.DELETED;
        };
    }
}

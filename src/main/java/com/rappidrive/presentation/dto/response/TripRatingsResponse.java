package com.rappidrive.presentation.dto.response;

import java.util.UUID;

/**
 * Response DTO para avaliações de uma viagem.
 */
public record TripRatingsResponse(
    UUID tripId,
    RatingResponse passengerRating,
    RatingResponse driverRating,
    Boolean canPassengerRate,
    Boolean canDriverRate
) {}

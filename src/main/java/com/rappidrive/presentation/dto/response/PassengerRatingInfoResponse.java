package com.rappidrive.presentation.dto.response;

import java.util.UUID;

/**
 * Response DTO para informações de rating de passageiro.
 */
public record PassengerRatingInfoResponse(
    UUID passengerId,
    Double averageRating,
    Long totalRatings
) {}

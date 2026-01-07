package com.rappidrive.presentation.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO para resumo de avaliações de motorista.
 */
public record DriverRatingSummaryResponse(
    UUID driverId,
    Double averageRating,
    Long totalRatings,
    Map<Integer, Long> ratingDistribution,
    List<RatingResponse> recentRatings
) {}

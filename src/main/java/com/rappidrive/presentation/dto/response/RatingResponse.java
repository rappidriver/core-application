package com.rappidrive.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO para avaliação.
 */
public record RatingResponse(
    UUID id,
    UUID tripId,
    UUID raterId,
    UUID rateeId,
    RatingTypeDto type,
    Integer score,
    String comment,
    RatingStatusDto status,
    LocalDateTime createdAt
) {
    
    /**
     * Enum DTO para tipo de avaliação.
     */
    public enum RatingTypeDto {
        DRIVER_BY_PASSENGER,
        PASSENGER_BY_DRIVER
    }
    
    /**
     * Enum DTO para status de avaliação.
     */
    public enum RatingStatusDto {
        ACTIVE,
        REPORTED,
        DELETED
    }
}

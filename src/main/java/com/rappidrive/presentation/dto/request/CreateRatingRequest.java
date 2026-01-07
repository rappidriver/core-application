package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Request para criar avaliação.
 */
public record CreateRatingRequest(
    
    @NotNull(message = "Trip ID é obrigatório")
    UUID tripId,
    
    @NotNull(message = "Type é obrigatório")
    RatingTypeDto type,
    
    @NotNull(message = "Score é obrigatório")
    @Min(value = 1, message = "Score mínimo é 1")
    @Max(value = 5, message = "Score máximo é 5")
    Integer score,
    
    @Size(max = 500, message = "Comentário deve ter no máximo 500 caracteres")
    String comment
) {
    
    /**
     * Enum DTO para tipo de avaliação.
     */
    public enum RatingTypeDto {
        DRIVER_BY_PASSENGER,
        PASSENGER_BY_DRIVER
    }
}

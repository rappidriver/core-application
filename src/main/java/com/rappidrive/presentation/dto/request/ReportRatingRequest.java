package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para reportar avaliação ofensiva.
 */
public record ReportRatingRequest(
    
    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    String reason
) {}

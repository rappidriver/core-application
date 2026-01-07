package com.rappidrive.presentation.dto.request;

import com.rappidrive.domain.enums.VehicleStatus;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de atualização de veículo.
 */
public record UpdateVehicleRequest(
    
    @Size(max = 30, message = "Color must not exceed 30 characters")
    String color,
    
    VehicleStatus status
) {
}

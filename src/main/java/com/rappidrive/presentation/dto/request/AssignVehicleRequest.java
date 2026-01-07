package com.rappidrive.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO para requisição de associação de veículo a motorista.
 */
public record AssignVehicleRequest(
    
    @NotNull(message = "Driver ID is required")
    UUID driverId
) {
}

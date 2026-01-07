package com.rappidrive.presentation.dto.response;

import com.rappidrive.domain.enums.VehicleStatus;
import com.rappidrive.domain.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para resposta de veículo.
 */
public record VehicleResponse(
    UUID id,
    UUID tenantId,
    UUID driverId,
    String licensePlate,
    String brand,
    String model,
    Integer year,
    String color,
    VehicleType type,
    Integer numberOfDoors,
    Integer seats,
    VehicleStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

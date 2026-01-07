package com.rappidrive.presentation.dto.request;

import com.rappidrive.domain.enums.VehicleType;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * DTO para requisição de criação de veículo.
 */
public record CreateVehicleRequest(
    
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,
    
    @NotBlank(message = "License plate is required")
    @Size(min = 7, max = 8, message = "License plate must be 7-8 characters")
    @Pattern(regexp = "^[A-Z]{3}[-]?[0-9]{4}$|^[A-Z]{3}[-]?[0-9][A-Z][0-9]{2}$", 
             message = "License plate must be in Brazilian format (ABC-1234 or ABC1D23)")
    String licensePlate,
    
    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    String brand,
    
    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    String model,
    
    @NotNull(message = "Year is required")
    @Min(value = 2015, message = "Vehicle cannot be older than 10 years")
    @Max(value = 2027, message = "Year cannot be in the future")
    Integer year,
    
    @NotBlank(message = "Color is required")
    @Size(max = 30, message = "Color must not exceed 30 characters")
    String color,
    
    @NotNull(message = "Vehicle type is required")
    VehicleType type,
    
    @NotNull(message = "Number of doors is required")
    @Min(value = 4, message = "Number of doors must be exactly 4")
    @Max(value = 4, message = "Number of doors must be exactly 4")
    Integer numberOfDoors,
    
    @NotNull(message = "Seats is required")
    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 12, message = "Seats must not exceed 12")
    Integer seats
) {
}

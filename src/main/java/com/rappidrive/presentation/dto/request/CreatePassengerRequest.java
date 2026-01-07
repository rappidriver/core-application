package com.rappidrive.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating a new passenger.
 */
@Schema(description = "Request to create a new passenger")
public record CreatePassengerRequest(
    
    @Schema(description = "Tenant ID for multi-tenancy", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "Tenant ID is required")
    @JsonProperty("tenantId")
    UUID tenantId,
    
    @Schema(description = "Passenger's full name", example = "Maria Santos", required = true)
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @JsonProperty("fullName")
    String fullName,
    
    @Schema(description = "Passenger's email address", example = "maria.santos@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    String email,
    
    @Schema(description = "Passenger's phone number", example = "+5511987654321", required = true)
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone format (E.164)")
    @JsonProperty("phone")
    String phone
) {}

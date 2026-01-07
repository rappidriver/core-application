package com.rappidrive.presentation.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * DTO for driver license information.
 */
@Schema(description = "Driver license information")
public record DriverLicenseDto(
    
    @Schema(description = "License number", example = "12345678901", required = true)
    @NotBlank(message = "License number is required")
    @Pattern(regexp = "\\d{11}", message = "License number must be 11 digits")
    @JsonProperty("number")
    String number,
    
    @Schema(description = "License category (A, B, C, D, E, AB, AC, AD, AE)", example = "B", required = true)
    @NotBlank(message = "License category is required")
    @Pattern(regexp = "^(A|B|C|D|E|AB|AC|AD|AE)$", message = "Invalid license category")
    @JsonProperty("category")
    String category,
    
    @Schema(description = "Issue date", example = "2020-01-01", required = true)
    @NotNull(message = "Issue date is required")
    @JsonProperty("issueDate")
    LocalDate issueDate,
    
    @Schema(description = "Expiry date", example = "2030-01-01", required = true)
    @NotNull(message = "Expiry date is required")
    @JsonProperty("expiryDate")
    LocalDate expiryDate,
    
    @Schema(description = "Is CNH definitive (true) or temporary/PPD (false)", example = "true", required = true)
    @NotNull(message = "Definitive status is required")
    @JsonProperty("isDefinitive")
    Boolean isDefinitive
) {}

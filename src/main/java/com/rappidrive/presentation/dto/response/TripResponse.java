package com.rappidrive.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rappidrive.presentation.dto.common.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for trip information.
 */
@Schema(description = "Trip information")
public record TripResponse(
    
    @Schema(description = "Trip ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("id")
    UUID id,
    
    @Schema(description = "Tenant ID", example = "123e4567-e89b-12d3-a456-426614174001")
    @JsonProperty("tenantId")
    UUID tenantId,
    
    @Schema(description = "Passenger ID", example = "123e4567-e89b-12d3-a456-426614174002")
    @JsonProperty("passengerId")
    UUID passengerId,
    
    @Schema(description = "Driver ID (if assigned)", example = "123e4567-e89b-12d3-a456-426614174003")
    @JsonProperty("driverId")
    UUID driverId,
    
    @Schema(description = "Origin location")
    @JsonProperty("origin")
    LocationDto origin,
    
    @Schema(description = "Destination location")
    @JsonProperty("destination")
    LocationDto destination,
    
    @Schema(description = "Trip status", example = "REQUESTED")
    @JsonProperty("status")
    String status,
    
    @Schema(description = "Estimated distance in kilometers", example = "5.8")
    @JsonProperty("estimatedDistanceKm")
    BigDecimal estimatedDistanceKm,
    
    @Schema(description = "Estimated fare amount", example = "25.50")
    @JsonProperty("estimatedFare")
    BigDecimal estimatedFare,
    
    @Schema(description = "Actual fare amount (after completion)", example = "27.00")
    @JsonProperty("actualFare")
    BigDecimal actualFare,
    
    @Schema(description = "Trip request timestamp", example = "2026-01-03T10:00:00")
    @JsonProperty("requestedAt")
    LocalDateTime requestedAt,
    
    @Schema(description = "Trip start timestamp", example = "2026-01-03T10:05:00")
    @JsonProperty("startedAt")
    LocalDateTime startedAt,
    
    @Schema(description = "Trip completion timestamp", example = "2026-01-03T10:25:00")
    @JsonProperty("completedAt")
    LocalDateTime completedAt,
    
    @Schema(description = "Fare ID (if calculated)", example = "123e4567-e89b-12d3-a456-426614174004")
    @JsonProperty("fareId")
    UUID fareId,
    
    @Schema(description = "Payment ID (if processed)", example = "123e4567-e89b-12d3-a456-426614174005")
    @JsonProperty("paymentId")
    UUID paymentId,
    
    @Schema(description = "Payment status", example = "PAID")
    @JsonProperty("paymentStatus")
    String paymentStatus
) {}

package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.valueobjects.TenantId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Input port for calculating trip fares.
 */
public interface CalculateFareInputPort {
    
    /**
     * Calculates the fare for a trip.
     *
     * @param command the calculation command
     * @return the calculated fare
     */
    Fare execute(CalculateFareCommand command);
    
    /**
     * Command for calculating a fare.
     */
    record CalculateFareCommand(
            UUID tripId,
            TenantId tenantId,
            double distanceKm,
            int durationMinutes,
            VehicleType vehicleCategory,
            LocalDateTime tripTime
    ) {
        public CalculateFareCommand {
            Objects.requireNonNull(tripId, "Trip ID cannot be null");
            Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
            Objects.requireNonNull(vehicleCategory, "Vehicle category cannot be null");
            Objects.requireNonNull(tripTime, "Trip time cannot be null");
            
            if (distanceKm < 0) {
                throw new IllegalArgumentException("Distance cannot be negative");
            }
            if (durationMinutes < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
        }
    }
}

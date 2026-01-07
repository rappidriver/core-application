package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.Objects;

/**
 * Input port for updating fare configurations.
 */
public interface UpdateFareConfigurationInputPort {
    
    /**
     * Updates or creates a fare configuration for a tenant.
     *
     * @param command the update command
     * @return the updated fare configuration
     */
    FareConfiguration execute(UpdateFareConfigurationCommand command);
    
    /**
     * Command for updating fare configuration.
     */
    record UpdateFareConfigurationCommand(
            TenantId tenantId,
            Money baseFare,
            Money pricePerKm,
            Money pricePerMinute,
            Money minimumFare,
            double platformCommissionRate
    ) {
        public UpdateFareConfigurationCommand {
            Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
            Objects.requireNonNull(baseFare, "Base fare cannot be null");
            Objects.requireNonNull(pricePerKm, "Price per km cannot be null");
            Objects.requireNonNull(pricePerMinute, "Price per minute cannot be null");
            Objects.requireNonNull(minimumFare, "Minimum fare cannot be null");
            
            if (baseFare.isNegativeOrZero()) {
                throw new IllegalArgumentException("Base fare must be positive");
            }
            if (pricePerKm.isNegativeOrZero()) {
                throw new IllegalArgumentException("Price per km must be positive");
            }
            if (pricePerMinute.isNegativeOrZero()) {
                throw new IllegalArgumentException("Price per minute must be positive");
            }
            if (minimumFare.isNegativeOrZero()) {
                throw new IllegalArgumentException("Minimum fare must be positive");
            }
            if (minimumFare.isLessThan(baseFare)) {
                throw new IllegalArgumentException("Minimum fare must be greater than or equal to base fare");
            }
            if (platformCommissionRate < 0.0 || platformCommissionRate > 1.0) {
                throw new IllegalArgumentException("Platform commission rate must be between 0.0 and 1.0");
            }
        }
    }
}

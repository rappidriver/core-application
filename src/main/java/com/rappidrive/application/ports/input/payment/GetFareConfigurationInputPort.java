package com.rappidrive.application.ports.input.payment;

import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.TenantId;

import java.util.Objects;

/**
 * Input port for retrieving fare configurations.
 */
public interface GetFareConfigurationInputPort {
    
    /**
     * Gets the fare configuration for a tenant.
     *
     * @param tenantId the tenant ID
     * @return the fare configuration
     */
    FareConfiguration execute(TenantId tenantId);
    
    /**
     * Command for getting fare configuration.
     */
    record GetFareConfigurationCommand(
            TenantId tenantId
    ) {
        public GetFareConfigurationCommand {
            Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        }
    }
}

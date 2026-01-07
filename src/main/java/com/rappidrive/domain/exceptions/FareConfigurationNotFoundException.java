package com.rappidrive.domain.exceptions;

import com.rappidrive.domain.valueobjects.TenantId;

/**
 * Exception thrown when a fare configuration is not found.
 */
public class FareConfigurationNotFoundException extends DomainException {
    
    public FareConfigurationNotFoundException(String message) {
        super(message);
    }
    
    public static FareConfigurationNotFoundException forTenant(TenantId tenantId) {
        return new FareConfigurationNotFoundException(
                "Fare configuration not found for tenant: " + tenantId.getValue());
    }
}

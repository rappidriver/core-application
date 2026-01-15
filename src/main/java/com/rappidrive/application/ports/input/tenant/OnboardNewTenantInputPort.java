package com.rappidrive.application.ports.input.tenant;

import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.TenantId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Input Port for tenant onboarding use case.
 * Follows Hexagonal Architecture - defines the contract for onboarding new tenants.
 */
public interface OnboardNewTenantInputPort {
    
    /**
     * Onboard a new tenant (city) into the RappiDrive platform.
     * 
     * This operation:
     * 1. Creates admin user in Keycloak
     * 2. Creates tenant group in Keycloak
     * 3. Saves fare configuration
     * 4. Saves service area (operating zone)
     * 5. Publishes TenantOnboardedEvent
     * 
     * @param command The onboarding command with all required data
     * @return OnboardingResult with tenant details and admin credentials
     * @throws com.rappidrive.application.exceptions.TenantAlreadyExistsException if tenant exists
     * @throws com.rappidrive.application.exceptions.IdentityProvisioningException if Keycloak operation fails
     */
    OnboardingResult execute(OnboardingCommand command);
    
    /**
     * Command for onboarding a new tenant.
     */
    record OnboardingCommand(
            TenantId tenantId,
            String displayName,
            Email adminEmail,
            String currency,
            BigDecimal baseFare,
            BigDecimal pricePerKm,
            BigDecimal pricePerMin,
            String serviceAreaName,
            String geoJsonPolygon
    ) {
        public OnboardingCommand {
            Objects.requireNonNull(tenantId, "TenantId cannot be null");
            Objects.requireNonNull(displayName, "DisplayName cannot be null");
            Objects.requireNonNull(adminEmail, "AdminEmail cannot be null");
            Objects.requireNonNull(currency, "Currency cannot be null");
            Objects.requireNonNull(baseFare, "BaseFare cannot be null");
            Objects.requireNonNull(pricePerKm, "PricePerKm cannot be null");
            Objects.requireNonNull(pricePerMin, "PricePerMin cannot be null");
            Objects.requireNonNull(serviceAreaName, "ServiceAreaName cannot be null");
            Objects.requireNonNull(geoJsonPolygon, "GeoJsonPolygon cannot be null");
            
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("DisplayName cannot be blank");
            }
            if (baseFare.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("BaseFare cannot be negative");
            }
            if (pricePerKm.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("PricePerKm cannot be negative");
            }
            if (pricePerMin.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("PricePerMin cannot be negative");
            }
        }
    }
    
    /**
     * Result of the onboarding operation.
     */
    record OnboardingResult(
            TenantId tenantId,
            String displayName,
            String adminEmail,
            String temporaryPassword,
            String keycloakUserId,
            String keycloakGroupId,
            String serviceAreaId,
            String fareConfigurationId,
            String message
    ) {
        public OnboardingResult {
            Objects.requireNonNull(tenantId, "TenantId cannot be null");
            Objects.requireNonNull(displayName, "DisplayName cannot be null");
            Objects.requireNonNull(adminEmail, "AdminEmail cannot be null");
            Objects.requireNonNull(temporaryPassword, "TemporaryPassword cannot be null");
            Objects.requireNonNull(keycloakUserId, "KeycloakUserId cannot be null");
            Objects.requireNonNull(keycloakGroupId, "KeycloakGroupId cannot be null");
            Objects.requireNonNull(serviceAreaId, "ServiceAreaId cannot be null");
            Objects.requireNonNull(fareConfigurationId, "FareConfigurationId cannot be null");
            Objects.requireNonNull(message, "Message cannot be null");
        }
    }
}

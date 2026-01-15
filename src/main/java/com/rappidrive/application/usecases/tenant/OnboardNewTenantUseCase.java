package com.rappidrive.application.usecases.tenant;

import com.rappidrive.application.exceptions.IdentityProvisioningException;
import com.rappidrive.application.exceptions.TenantAlreadyExistsException;
import com.rappidrive.application.ports.input.tenant.OnboardNewTenantInputPort;
import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.application.ports.output.IdentityProvisioningPort;
import com.rappidrive.application.ports.output.ServiceAreaRepositoryPort;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.entities.ServiceArea;
import com.rappidrive.domain.events.DomainEventsCollector;
import com.rappidrive.domain.events.TenantOnboardedEvent;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * OnboardNewTenantUseCase - Orchestrates the complete tenant onboarding flow.
 * 
 * Responsibilities:
 * 1. Validate tenant doesn't already exist
 * 2. Create admin user in Keycloak
 * 3. Create tenant group in Keycloak
 * 4. Save fare configuration
 * 5. Save service area (operating zone)
 * 6. Publish TenantOnboardedEvent for observers (email, notifications, etc.)
 * 
 * Follows Hexagonal Architecture:
 * - Depends only on output ports (interfaces)
 * - No direct infrastructure code
 * - Pure business logic orchestration
 * - All dependencies via constructor injection (testable)
 */

public class OnboardNewTenantUseCase implements OnboardNewTenantInputPort {
    
    private static final Logger log = LoggerFactory.getLogger(OnboardNewTenantUseCase.class);
    private static final int TEMPORARY_PASSWORD_LENGTH = 16;
    
    private final ServiceAreaRepositoryPort serviceAreaRepository;
    private final FareConfigurationRepositoryPort fareConfigRepository;
    private final IdentityProvisioningPort identityProvisioning;
    private final com.rappidrive.application.ports.output.TenantRepositoryPort tenantRepository;
    
    public OnboardNewTenantUseCase(ServiceAreaRepositoryPort serviceAreaRepository,
                                  FareConfigurationRepositoryPort fareConfigRepository,
                                  IdentityProvisioningPort identityProvisioning,
                                  com.rappidrive.application.ports.output.TenantRepositoryPort tenantRepository) {
        this.serviceAreaRepository = serviceAreaRepository;
        this.fareConfigRepository = fareConfigRepository;
        this.identityProvisioning = identityProvisioning;
        this.tenantRepository = tenantRepository;
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional
    public OnboardingResult execute(OnboardingCommand command) {
        log.info("Starting tenant onboarding: tenantId={}, displayName={}", 
                command.tenantId(), command.displayName());
        
        try {
            // Step 1: Validate that tenant does not have existing data in DB (fast fail)
            if (serviceAreaRepository.existsByTenantId(command.tenantId())) {
                throw new TenantAlreadyExistsException(command.tenantId().getValue().toString());
            }

            // Step 2: Generate temporary password
            String tempPassword = generateTemporaryPassword();

            // Validate inputs first (fail fast before calling external systems)
            log.info("Validating fare configuration and service area for tenant: {}", command.tenantId());
            Currency currency = Currency.valueOf(command.currency().toUpperCase());
            FareConfiguration fareConfig = FareConfiguration.create(
                    command.tenantId(),
                    new Money(command.baseFare(), currency),
                    new Money(command.pricePerKm(), currency),
                    new Money(command.pricePerMin(), currency),
                    new Money(command.baseFare().add(java.math.BigDecimal.valueOf(0.01)), currency),
                    0.15
            );

            ServiceArea serviceArea = ServiceArea.create(
                    command.tenantId(),
                    command.serviceAreaName(),
                    command.geoJsonPolygon()
            );

            // Step 3: Persist tenant record in DB (so foreign key constraints succeed)
            log.info("Creating tenant record in DB: {}", command.tenantId());
            String slug = command.displayName().toLowerCase().replaceAll("[^a-z0-9-]","-");
            com.rappidrive.domain.entities.Tenant tenant = com.rappidrive.domain.entities.Tenant.create(
                    command.tenantId(), command.displayName(), slug
            );
            tenantRepository.save(tenant);
            // Ensure tenant persistence succeeded before continuing (helps diagnose FK issues)
            if (!tenantRepository.existsById(tenant.getId())) {
                log.error("Tenant record was not persisted for id={}", tenant.getId());
                throw new RuntimeException("Tenant persistence failure for id: " + tenant.getId());
            }

            // Check identity provider for existing tenant group (must be done after basic validation)
            if (identityProvisioning.tenantGroupExists(command.tenantId())) {
                throw new TenantAlreadyExistsException(command.tenantId().getValue().toString());
            }

            // Step 4: Create admin in Keycloak
            log.info("Creating admin user in Keycloak for tenant: {}", command.tenantId());
            String keycloakUserId = identityProvisioning.createTenantAdmin(
                    command.tenantId(),
                    command.adminEmail(),
                    tempPassword
            );
            log.info("Admin created in Keycloak with ID: {}", keycloakUserId);

            // Step 5: Create tenant group in Keycloak
            log.info("Creating tenant group in Keycloak");
            String keycloakGroupId = identityProvisioning.createTenantGroup(command.tenantId());
            log.info("Tenant group created with ID: {}", keycloakGroupId);

            // Step 5: Save fare configuration and service area
            log.info("Saving fare configuration and service area for tenant: {}", command.tenantId());
            FareConfiguration savedFareConfig = fareConfigRepository.save(fareConfig);
            log.info("Fare configuration saved with ID: {}", savedFareConfig.getId());

            ServiceArea savedServiceArea = serviceAreaRepository.save(serviceArea);
            log.info("Service area saved with ID: {}", savedServiceArea.getId());
            
            // Step 7: Publish domain event
            log.info("Publishing TenantOnboardedEvent");
            TenantOnboardedEvent event = TenantOnboardedEvent.builder()
                    .tenantId(command.tenantId())
                    .displayName(command.displayName())
                    .adminEmail(command.adminEmail().getValue())
                    .serviceAreaName(command.serviceAreaName())
                    .build();
            
            DomainEventsCollector.instance().handle(event);
            
            // Step 8: Build and return result
            OnboardingResult result = new OnboardingResult(
                    command.tenantId(),
                    command.displayName(),
                    command.adminEmail().getValue(),
                    tempPassword,
                    keycloakUserId,
                    keycloakGroupId,
                    savedServiceArea.getId().getValue().toString(),
                    savedFareConfig.getId().toString(),
                    String.format(
                            "Tenant '%s' onboarded successfully. Admin user created: %s. " +
                            "User must change temporary password on first login.",
                            command.displayName(),
                            command.adminEmail()
                    )
            );
            
            log.info("Tenant onboarding completed successfully: {}", command.tenantId());
            return result;
            
        } catch (TenantAlreadyExistsException e) {
            log.warn("Tenant already exists: {}", command.tenantId());
            throw e;
        } catch (IdentityProvisioningException e) {
            log.error("Identity provisioning failed during tenant onboarding", e);
            // Clean up created resources if needed
            cleanupOnboardingOnError(command.tenantId());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during tenant onboarding", e);
            cleanupOnboardingOnError(command.tenantId());
            throw new RuntimeException("Tenant onboarding failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate that tenant doesn't already exist.
     */
    private void validateTenantDoesNotExist(TenantId tenantId) {
        if (serviceAreaRepository.existsByTenantId(tenantId)) {
            throw new TenantAlreadyExistsException(tenantId.getValue().toString());
        }
        
        if (identityProvisioning.tenantGroupExists(tenantId)) {
            throw new TenantAlreadyExistsException(tenantId.getValue().toString());
        }
    }
    
    /**
     * Generate a secure temporary password.
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TEMPORARY_PASSWORD_LENGTH];
        random.nextBytes(bytes);
        
        // Use URL-safe Base64 to ensure password is printable and URL-friendly
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes)
                .substring(0, TEMPORARY_PASSWORD_LENGTH);
    }
    
    /**
     * Clean up resources if onboarding fails.
     * This is a best-effort cleanup - errors are logged but not propagated.
     */
    private void cleanupOnboardingOnError(TenantId tenantId) {
        try {
            log.info("Cleaning up tenant resources after error: {}", tenantId);
            
            // Attempt to delete tenant group from Keycloak
            try {
                identityProvisioning.deleteTenantGroup(tenantId);
                log.info("Tenant group deleted during cleanup");
            } catch (Exception e) {
                log.error("Error deleting tenant group during cleanup", e);
            }
            
            // Attempt to delete service area
            try {
                serviceAreaRepository.findByTenantId(tenantId)
                        .forEach(sa -> serviceAreaRepository.delete(sa.getId()));
                log.info("Service areas deleted during cleanup");
            } catch (Exception e) {
                log.error("Error deleting service areas during cleanup", e);
            }

            // Attempt to delete tenant record
            try {
                tenantRepository.deleteById(tenantId);
                log.info("Tenant record deleted during cleanup");
            } catch (Exception e) {
                log.error("Error deleting tenant record during cleanup", e);
            }

            // Note: FareConfiguration is typically kept for audit purposes
            // even if onboarding fails, so we don't delete it

        } catch (Exception e) {
            log.error("Error during cleanup after onboarding failure", e);
        }
    }
}

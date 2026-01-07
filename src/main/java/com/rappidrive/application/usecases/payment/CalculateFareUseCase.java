package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.CalculateFareInputPort;
import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.exceptions.FareConfigurationNotFoundException;

/**
 * Use case for calculating trip fares.
 */
public class CalculateFareUseCase implements CalculateFareInputPort {
    
    private final FareConfigurationRepositoryPort fareConfigurationRepository;
    
    public CalculateFareUseCase(FareConfigurationRepositoryPort fareConfigurationRepository) {
        this.fareConfigurationRepository = fareConfigurationRepository;
    }
    
    @Override
    public Fare execute(CalculateFareCommand command) {
        // Get fare configuration for tenant
        FareConfiguration config = fareConfigurationRepository.findByTenantId(command.tenantId())
                .orElseThrow(() -> FareConfigurationNotFoundException.forTenant(command.tenantId()));
        
        // Calculate fare using domain entity
        return Fare.calculate(
                config,
                command.tripId(),
                command.tenantId(),
                command.distanceKm(),
                command.durationMinutes(),
                command.vehicleCategory(),
                command.tripTime()
        );
    }
}

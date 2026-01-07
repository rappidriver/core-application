package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.UpdateFareConfigurationInputPort;
import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.domain.entities.FareConfiguration;

/**
 * Use case for updating fare configurations.
 */
public class UpdateFareConfigurationUseCase implements UpdateFareConfigurationInputPort {
    
    private final FareConfigurationRepositoryPort fareConfigurationRepository;
    
    public UpdateFareConfigurationUseCase(FareConfigurationRepositoryPort fareConfigurationRepository) {
        this.fareConfigurationRepository = fareConfigurationRepository;
    }
    
    @Override
    public FareConfiguration execute(UpdateFareConfigurationCommand command) {
        // Find existing configuration or create new one
        FareConfiguration config = fareConfigurationRepository.findByTenantId(command.tenantId())
                .orElseGet(() -> FareConfiguration.create(
                        command.tenantId(),
                        command.baseFare(),
                        command.pricePerKm(),
                        command.pricePerMinute(),
                        command.minimumFare(),
                        command.platformCommissionRate()
                ));
        
        // Update if exists
        if (fareConfigurationRepository.existsByTenantId(command.tenantId())) {
            config.updateBaseFare(command.baseFare());
            config.updatePricePerKm(command.pricePerKm());
            config.updatePricePerMinute(command.pricePerMinute());
            config.updateMinimumFare(command.minimumFare());
            config.updateCommissionRate(command.platformCommissionRate());
        }
        
        // Save and return
        return fareConfigurationRepository.save(config);
    }
}

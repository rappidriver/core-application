package com.rappidrive.application.usecases.payment;

import com.rappidrive.application.ports.input.payment.GetFareConfigurationInputPort;
import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.exceptions.FareConfigurationNotFoundException;
import com.rappidrive.domain.valueobjects.TenantId;

/**
 * Use case for getting fare configurations.
 */
public class GetFareConfigurationUseCase implements GetFareConfigurationInputPort {
    
    private final FareConfigurationRepositoryPort fareConfigurationRepository;
    
    public GetFareConfigurationUseCase(FareConfigurationRepositoryPort fareConfigurationRepository) {
        this.fareConfigurationRepository = fareConfigurationRepository;
    }
    
    @Override
    public FareConfiguration execute(TenantId tenantId) {
        return fareConfigurationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> FareConfigurationNotFoundException.forTenant(tenantId));
    }
}

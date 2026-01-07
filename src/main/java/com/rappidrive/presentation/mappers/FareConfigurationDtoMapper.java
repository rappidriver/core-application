package com.rappidrive.presentation.mappers;

import com.rappidrive.application.ports.input.payment.UpdateFareConfigurationInputPort.UpdateFareConfigurationCommand;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.presentation.dto.request.UpdateFareConfigurationRequest;
import com.rappidrive.presentation.dto.response.FareConfigurationResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for conversion between DTOs and Commands/Entities for fare configuration.
 */
@Component
public class FareConfigurationDtoMapper {
    
    /**
     * Converts UpdateFareConfigurationRequest to UpdateFareConfigurationCommand.
     */
    public UpdateFareConfigurationCommand toCommand(UpdateFareConfigurationRequest request) {
        return new UpdateFareConfigurationCommand(
            new TenantId(request.tenantId()),
            new Money(request.baseFare()),
            new Money(request.pricePerKm()),
            new Money(request.pricePerMinute()),
            new Money(request.minimumFare()),
            request.platformCommissionRate()
        );
    }
    
    /**
     * Converts FareConfiguration entity to FareConfigurationResponse DTO.
     */
    public FareConfigurationResponse toResponse(FareConfiguration fareConfig) {
        return new FareConfigurationResponse(
            fareConfig.getId(),
            fareConfig.getTenantId().getValue(),
            fareConfig.getBaseFare().getAmount(),
            fareConfig.getPricePerKm().getAmount(),
            fareConfig.getPricePerMinute().getAmount(),
            fareConfig.getMinimumFare().getAmount(),
            fareConfig.getPlatformCommissionRate(),
            fareConfig.getCreatedAt(),
            fareConfig.getUpdatedAt()
        );
    }
}

package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.FareConfigurationJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for conversion between FareConfiguration (domain) and FareConfigurationJpaEntity (JPA).
 * Manual implementation due to final fields and value objects.
 */
@Component
public class FareConfigurationMapper {
    
    /**
     * Converts domain FareConfiguration to JPA entity.
     */
    public FareConfigurationJpaEntity toJpaEntity(FareConfiguration fareConfig) {
        return new FareConfigurationJpaEntity(
            fareConfig.getId(),
            fareConfig.getTenantId().getValue(),
            fareConfig.getBaseFare().getAmount(),
            fareConfig.getPricePerKm().getAmount(),
            fareConfig.getPricePerMinute().getAmount(),
            fareConfig.getMinimumFare().getAmount(),
            BigDecimal.valueOf(fareConfig.getPlatformCommissionRate()),
            fareConfig.getCreatedAt(),
            fareConfig.getUpdatedAt()
        );
    }
    
    /**
     * Converts JPA entity to domain FareConfiguration.
     */
    public FareConfiguration toDomain(FareConfigurationJpaEntity entity) {
        return new FareConfiguration(
            entity.getId(),
            new TenantId(entity.getTenantId()),
            new Money(entity.getBaseFare()),
            new Money(entity.getPricePerKm()),
            new Money(entity.getPricePerMinute()),
            new Money(entity.getMinimumFare()),
            entity.getPlatformCommissionRate().doubleValue(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

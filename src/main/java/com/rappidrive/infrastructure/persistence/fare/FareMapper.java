package com.rappidrive.infrastructure.persistence.fare;

import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.enums.FareMultiplierType;
import com.rappidrive.domain.enums.VehicleType;
import com.rappidrive.domain.valueobjects.Currency;
import com.rappidrive.domain.valueobjects.FareBreakdown;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.FareJpaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper for converting between Fare domain entity and FareJpaEntity.
 */
@Component
public class FareMapper {
    
    /**
     * Converts Fare domain entity to JPA entity.
     */
    public FareJpaEntity toJpaEntity(Fare fare) {
        if (fare == null) {
            return null;
        }
        
        FareJpaEntity entity = new FareJpaEntity();
        entity.setId(fare.getId());
        entity.setTripId(fare.getTripId());
        entity.setTenantId(fare.getTenantId().getValue());
        entity.setBaseFare(fare.getBaseFare().getAmount());
        entity.setDistanceKm(fare.getDistanceKm());
        entity.setDurationMinutes(fare.getDurationMinutes());
        entity.setDistanceFare(fare.getDistanceFare().getAmount());
        entity.setTimeFare(fare.getTimeFare().getAmount());
        entity.setMultiplierType(fare.getMultiplierType());
        entity.setTimeMultiplier(BigDecimal.valueOf(fare.getBreakdown().getTimeMultiplier()));
        entity.setVehicleCategory(fare.getVehicleCategory());
        entity.setVehicleMultiplier(BigDecimal.valueOf(fare.getBreakdown().getVehicleMultiplier()));
        entity.setTotalBeforeMultiplier(fare.getTotalBeforeMultiplier().getAmount());
        entity.setTotalAmount(fare.getTotalAmount().getAmount());
        entity.setMinimumFare(fare.getBreakdown().getMinimumFare().getAmount());
        entity.setExplanation(fare.getBreakdown().getExplanation());
        entity.setCurrency(fare.getTotalAmount().getCurrency().name());
        entity.setCalculatedAt(fare.getCalculatedAt());
        
        return entity;
    }
    
    /**
     * Converts FareJpaEntity to Fare domain entity.
     */
    public Fare toDomain(FareJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Currency currency = Currency.valueOf(entity.getCurrency());
        
        Money baseFare = new Money(entity.getBaseFare(), currency);
        Money distanceFare = new Money(entity.getDistanceFare(), currency);
        Money timeFare = new Money(entity.getTimeFare(), currency);
        Money totalBeforeMultiplier = new Money(entity.getTotalBeforeMultiplier(), currency);
        Money totalAmount = new Money(entity.getTotalAmount(), currency);
        Money minimumFare = new Money(entity.getMinimumFare(), currency);
        
        FareBreakdown breakdown = new FareBreakdown(
            baseFare,
            distanceFare,
            timeFare,
            entity.getVehicleMultiplier().doubleValue(),
            entity.getTimeMultiplier().doubleValue(),
            minimumFare,
            totalAmount
        );
        
        return new Fare(
            entity.getId(),
            entity.getTripId(),
            new TenantId(entity.getTenantId()),
            baseFare,
            entity.getDistanceKm(),
            entity.getDurationMinutes(),
            distanceFare,
            timeFare,
            entity.getMultiplierType(),
            entity.getVehicleCategory(),
            totalBeforeMultiplier,
            totalAmount,
            breakdown,
            entity.getCalculatedAt()
        );
    }
}

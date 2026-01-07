package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.Trip;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.Money;
import com.rappidrive.domain.valueobjects.TripId;
import com.rappidrive.domain.valueobjects.PassengerId;
import com.rappidrive.domain.valueobjects.DriverId;
import com.rappidrive.infrastructure.persistence.entities.TripJpaEntity;
import org.mapstruct.*;

import java.math.BigDecimal;

/**
 * MapStruct mapper for converting between Trip domain entity and TripJpaEntity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TripMapper {
    
    /**
     * Maps JPA entity to domain entity using reconstruction constructor.
     */
    default Trip toDomain(TripJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Location origin = new Location(entity.getPickupLatitude(), entity.getPickupLongitude());
        Location destination = new Location(entity.getDropoffLatitude(), entity.getDropoffLongitude());

        return new Trip(
            new TripId(entity.getId()),
            entity.getTenantId(),
            new PassengerId(entity.getPassengerId()),
            entity.getDriverId() != null ? new DriverId(entity.getDriverId()) : null,
            origin,
            destination,
            entity.getStatus(),
            entity.getRequestedAt(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getFareId(),
            entity.getPaymentId(),
            entity.getPaymentStatus() != null ? com.rappidrive.domain.trip.TripPaymentStatus.valueOf(entity.getPaymentStatus()) : null
        );
    }

    /**
     * Maps domain entity to JPA entity.
     */
    default TripJpaEntity toJpaEntity(Trip domain) {
        if (domain == null) {
            return null;
        }

        TripJpaEntity entity = new TripJpaEntity();
        entity.setId(domain.getId().getValue());
        entity.setTenantId(domain.getTenantId());
        entity.setPassengerId(domain.getPassengerId().getValue());
        entity.setDriverId(domain.getDriverId().orElse(null));
        entity.setPickupLatitude(domain.getOrigin().getLatitude());
        entity.setPickupLongitude(domain.getOrigin().getLongitude());
        entity.setDropoffLatitude(domain.getDestination().getLatitude());
        entity.setDropoffLongitude(domain.getDestination().getLongitude());
        entity.setStatus(domain.getStatus());
        entity.setRequestedAt(domain.getRequestedAt());
        entity.setStartedAt(domain.getStartedAt().orElse(null));
        entity.setCompletedAt(domain.getCompletedAt().orElse(null));
        entity.setFareId(domain.getFareId().orElse(null));
        entity.setPaymentId(domain.getPaymentId().orElse(null));
        entity.setPaymentStatus(domain.getPaymentStatus() != null ? domain.getPaymentStatus().name() : null);
        return entity;
    }
    
    /**
     * Updates an existing JPA entity from domain entity.
     */
    default void updateJpaEntity(TripJpaEntity entity, Trip domain) {
        if (entity == null || domain == null) {
            return;
        }
        
        entity.setDriverId(domain.getDriverId().orElse(null));
        entity.setStatus(domain.getStatus());
        entity.setDistanceKm(domain.getEstimatedDistanceKm());
        entity.setFareAmount(domain.getActualFare().map(Money::getAmount).orElse(null));
        entity.setFareId(domain.getFareId().orElse(null));
        entity.setPaymentId(domain.getPaymentId().orElse(null));
        entity.setPaymentStatus(domain.getPaymentStatus() != null ? domain.getPaymentStatus().name() : null);
        entity.setStartedAt(domain.getStartedAt().orElse(null));
        entity.setCompletedAt(domain.getCompletedAt().orElse(null));
    }
}


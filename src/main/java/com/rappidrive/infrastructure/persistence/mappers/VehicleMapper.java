package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.VehicleYear;
import com.rappidrive.infrastructure.persistence.entities.VehicleJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversão entre Vehicle (domain) e VehicleJpaEntity (JPA).
 * Implementação manual devido aos campos finais do Vehicle.
 */
@Component
public class VehicleMapper {
    
    /**
     * Converte domain Vehicle para JPA entity.
     */
    public VehicleJpaEntity toJpaEntity(Vehicle vehicle) {
        return new VehicleJpaEntity(
            vehicle.getId(),
            vehicle.getTenantId().getValue(),
            vehicle.getDriverId(),
            vehicle.getLicensePlate().getValue(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getYear().getValue(),
            vehicle.getColor(),
            vehicle.getType(),
            vehicle.getNumberOfDoors(),
            vehicle.getSeats(),
            vehicle.getStatus(),
            vehicle.getCreatedAt(),
            vehicle.getUpdatedAt()
        );
    }
    
    /**
     * Converte JPA entity para domain Vehicle.
     */
    public Vehicle toDomain(VehicleJpaEntity entity) {
        return new Vehicle(
            entity.getId(),
            new TenantId(entity.getTenantId()),
            entity.getDriverId(),
            new LicensePlate(entity.getLicensePlate()),
            entity.getBrand(),
            entity.getModel(),
            new VehicleYear(entity.getYear()),
            entity.getColor(),
            entity.getType(),
            entity.getNumberOfDoors(),
            entity.getSeats(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

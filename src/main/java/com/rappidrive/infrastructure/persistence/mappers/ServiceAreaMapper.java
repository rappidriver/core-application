package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.ServiceArea;
import com.rappidrive.domain.valueobjects.ServiceAreaId;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.ServiceAreaJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper between ServiceArea domain entity and ServiceAreaJpaEntity.
 * No framework dependencies in domain layer - mapping happens in infrastructure.
 */
@Component
public class ServiceAreaMapper {
    
    /**
     * Convert JPA entity to domain entity.
     */
    public ServiceArea toDomain(ServiceAreaJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return new ServiceArea.Builder()
                .id(ServiceAreaId.of(jpaEntity.getId()))
                .tenantId(TenantId.fromString(jpaEntity.getTenantId().toString()))
                .name(jpaEntity.getName())
                .geoJsonPolygon(jpaEntity.getGeoJsonPolygon())
                .active(jpaEntity.isActive())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert domain entity to JPA entity.
     */
    public ServiceAreaJpaEntity toJpaEntity(ServiceArea domain) {
        if (domain == null) {
            return null;
        }
        
        return new ServiceAreaJpaEntity(
                domain.getId().getValue(),
                domain.getTenantId().getValue(),
                domain.getName(),
                domain.getGeoJsonPolygon(),
                domain.isActive(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}

package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.valueobjects.TenantId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

/**
 * JPA converter for TenantId value object.
 */
@Converter(autoApply = true)
public class TenantIdConverter implements AttributeConverter<TenantId, UUID> {
    
    @Override
    public UUID convertToDatabaseColumn(TenantId tenantId) {
        return tenantId == null ? null : tenantId.getValue();
    }
    
    @Override
    public TenantId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new TenantId(dbData);
    }
}

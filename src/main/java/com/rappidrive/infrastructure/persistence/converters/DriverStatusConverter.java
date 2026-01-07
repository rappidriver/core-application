package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.enums.DriverStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for DriverStatus enum.
 */
@Converter(autoApply = true)
public class DriverStatusConverter implements AttributeConverter<DriverStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(DriverStatus status) {
        return status == null ? null : status.name();
    }
    
    @Override
    public DriverStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DriverStatus.valueOf(dbData);
    }
}

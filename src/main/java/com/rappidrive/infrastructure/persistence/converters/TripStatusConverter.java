package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.enums.TripStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for TripStatus enum.
 */
@Converter(autoApply = true)
public class TripStatusConverter implements AttributeConverter<TripStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(TripStatus status) {
        return status == null ? null : status.name();
    }
    
    @Override
    public TripStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TripStatus.valueOf(dbData);
    }
}

package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.enums.PassengerStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for PassengerStatus enum.
 */
@Converter(autoApply = true)
public class PassengerStatusConverter implements AttributeConverter<PassengerStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(PassengerStatus status) {
        return status == null ? null : status.name();
    }
    
    @Override
    public PassengerStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PassengerStatus.valueOf(dbData);
    }
}

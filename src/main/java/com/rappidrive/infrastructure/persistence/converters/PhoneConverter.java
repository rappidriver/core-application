package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.valueobjects.Phone;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for Phone value object.
 */
@Converter(autoApply = true)
public class PhoneConverter implements AttributeConverter<Phone, String> {
    
    @Override
    public String convertToDatabaseColumn(Phone phone) {
        return phone == null ? null : phone.getValue();
    }
    
    @Override
    public Phone convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Phone(dbData);
    }
}

package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.valueobjects.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for Email value object.
 */
@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {
    
    @Override
    public String convertToDatabaseColumn(Email email) {
        return email == null ? null : email.getValue();
    }
    
    @Override
    public Email convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Email(dbData);
    }
}

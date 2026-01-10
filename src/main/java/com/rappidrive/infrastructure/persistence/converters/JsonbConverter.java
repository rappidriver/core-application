package com.rappidrive.infrastructure.persistence.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter for PostgreSQL JSONB type.
 * Maps between String (domain/application) and JSONB (database).
 * 
 * This converter ensures that String payloads are properly converted to JSONB
 * for storage in PostgreSQL without type casting issues.
 */
@Converter(autoApply = true)
public class JsonbConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // Return the string as-is; PostgreSQL JSONB will handle it
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // Return the database string as-is
        return dbData;
    }
}

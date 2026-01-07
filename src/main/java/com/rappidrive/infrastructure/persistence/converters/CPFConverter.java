package com.rappidrive.infrastructure.persistence.converters;

import com.rappidrive.domain.valueobjects.CPF;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for CPF value object.
 */
@Converter(autoApply = true)
public class CPFConverter implements AttributeConverter<CPF, String> {
    
    @Override
    public String convertToDatabaseColumn(CPF cpf) {
        return cpf == null ? null : cpf.getValue();
    }
    
    @Override
    public CPF convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new CPF(dbData);
    }
}

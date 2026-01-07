package com.rappidrive.presentation.mappers;

import com.rappidrive.domain.entities.Passenger;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.Phone;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.presentation.dto.response.PassengerResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Passenger DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface PassengerDtoMapper {
    
    /**
     * Converts string to Email value object.
     */
    default Email toEmail(String email) {
        return email != null ? new Email(email) : null;
    }
    
    /**
     * Converts string to Phone value object.
     */
    default Phone toPhone(String phone) {
        return phone != null ? new Phone(phone) : null;
    }
    
    /**
     * Converts UUID to TenantId value object.
     */
    default TenantId toTenantId(java.util.UUID tenantId) {
        return tenantId != null ? new TenantId(tenantId) : null;
    }
    
    /**
     * Converts Passenger domain entity to PassengerResponse.
     */
    default PassengerResponse toResponse(Passenger passenger) {
        if (passenger == null) return null;
        
        return new PassengerResponse(
            passenger.getId(),
            passenger.getTenantId().getValue(),
            passenger.getFullName(),
            passenger.getEmail().getValue(),
            passenger.getPhone().getValue(),
            passenger.getStatus().name(),
            passenger.getCreatedAt(),
            passenger.getUpdatedAt()
        );
    }
}

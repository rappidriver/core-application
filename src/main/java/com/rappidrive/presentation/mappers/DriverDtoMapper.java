package com.rappidrive.presentation.mappers;

import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.*;
import com.rappidrive.presentation.dto.common.DriverLicenseDto;
import com.rappidrive.presentation.dto.common.LocationDto;
import com.rappidrive.presentation.dto.request.CreateDriverRequest;
import com.rappidrive.presentation.dto.response.DriverResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Driver DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface DriverDtoMapper {
    
    /**
     * Converts CreateDriverRequest to domain entities/value objects for use case command.
     */
    default Email toEmail(String email) {
        return email != null ? new Email(email) : null;
    }
    
    default CPF toCPF(String cpf) {
        return cpf != null ? new CPF(cpf) : null;
    }
    
    default Phone toPhone(String phone) {
        return phone != null ? new Phone(phone) : null;
    }
    
    default TenantId toTenantId(java.util.UUID tenantId) {
        return tenantId != null ? new TenantId(tenantId) : null;
    }
    
    default DriverLicense toDriverLicense(DriverLicenseDto dto) {
        if (dto == null) return null;
        return new DriverLicense(
            dto.number(),
            dto.category(),
            dto.issueDate(),
            dto.expiryDate(),
            dto.isDefinitive()
        );
    }
    
    default Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        return new Location(dto.latitude(), dto.longitude());
    }
    
    default Location toLocation(double latitude, double longitude) {
        return new Location(latitude, longitude);
    }
    
    /**
     * Converts Driver domain entity to DriverResponse.
     */
    default DriverResponse toResponse(Driver driver) {
        if (driver == null) return null;
        
        return new DriverResponse(
            driver.getId(),
            driver.getTenantId().getValue(),
            driver.getFullName(),
            driver.getEmail().getValue(),
            driver.getCpf().getValue(),
            driver.getPhone().getValue(),
            driver.getStatus().name(),
            driver.getCurrentLocation().map(this::toLocationDto).orElse(null),
            driver.getCreatedAt(),
            driver.getUpdatedAt()
        );
    }
    
    default LocationDto toLocationDto(Location location) {
        if (location == null) return null;
        return new LocationDto(location.getLatitude(), location.getLongitude());
    }
}

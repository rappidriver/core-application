package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.DriverGeoQueryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.enums.DriverStatus;
import com.rappidrive.domain.valueobjects.CPF;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.DriverJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.DriverMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataDriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementation of DriverRepositoryPort and DriverGeoQueryPort.
 * This adapter converts between domain entities and JPA entities.
 * Implements geospatial queries using PostGIS.
 */
@Component
@RequiredArgsConstructor
public class JpaDriverRepositoryAdapter implements DriverRepositoryPort, DriverGeoQueryPort {
    
    private final SpringDataDriverRepository jpaRepository;
    private final DriverMapper mapper;
    
    @Override
    public Driver save(Driver driver) {
        DriverJpaEntity entity;
        
        if (driver.getId() != null) {
            // Update existing
            entity = jpaRepository.findById(driver.getId())
                .orElseGet(() -> mapper.toJpaEntity(driver));
            mapper.updateJpaEntity(entity, driver);
        } else {
            // Create new
            entity = mapper.toJpaEntity(driver);
        }
        
        DriverJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Driver> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Driver> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Optional<Driver> findByEmail(Email email) {
        return jpaRepository.findByEmail(email)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Driver> findByCpf(CPF cpf) {
        return jpaRepository.findByCpf(cpf)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Driver> findByStatus(DriverStatus status, TenantId tenantId) {
        return jpaRepository.findByStatusAndTenantId(status, tenantId).stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByCpf(CPF cpf) {
        return jpaRepository.existsByCpf(cpf);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    // DriverGeoQueryPort implementation
    
    @Override
    public List<Driver> findAvailableDriversNearby(Location pickupLocation, double radiusKm, TenantId tenantId) {
        return jpaRepository.findDriversWithinRadius(
                pickupLocation.getLatitude(),
                pickupLocation.getLongitude(),
                radiusKm * 1000, // Convert km to meters for PostGIS
                tenantId
            )
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
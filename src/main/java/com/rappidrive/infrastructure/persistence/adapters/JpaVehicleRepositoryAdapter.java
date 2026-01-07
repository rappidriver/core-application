package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.VehicleRepositoryPort;
import com.rappidrive.domain.entities.Vehicle;
import com.rappidrive.domain.valueobjects.LicensePlate;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.VehicleJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.VehicleMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataVehicleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementando VehicleRepositoryPort usando JPA.
 */
@Component
public class JpaVehicleRepositoryAdapter implements VehicleRepositoryPort {
    
    private final SpringDataVehicleRepository jpaRepository;
    private final VehicleMapper mapper;
    
    public JpaVehicleRepositoryAdapter(SpringDataVehicleRepository jpaRepository, VehicleMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleJpaEntity entity = mapper.toJpaEntity(vehicle);
        VehicleJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Vehicle> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Vehicle> findByDriverId(UUID driverId) {
        return jpaRepository.findByDriverId(driverId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Vehicle> findActiveByDriverId(UUID driverId) {
        return jpaRepository.findActiveByDriverId(driverId)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Vehicle> findByLicensePlate(LicensePlate licensePlate, TenantId tenantId) {
        return jpaRepository.findByLicensePlateAndTenantId(
                licensePlate.getValue(),
                tenantId.getValue()
            ).map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByLicensePlate(LicensePlate licensePlate, TenantId tenantId) {
        return jpaRepository.existsByLicensePlateAndTenantId(
            licensePlate.getValue(),
            tenantId.getValue()
        );
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}

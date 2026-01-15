package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.ServiceAreaRepositoryPort;
import com.rappidrive.domain.entities.ServiceArea;
import com.rappidrive.domain.valueobjects.ServiceAreaId;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.mappers.ServiceAreaMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataServiceAreaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA adapter implementation of ServiceAreaRepositoryPort.
 * Bridges domain layer and infrastructure persistence.
 */
@Component
public class JpaServiceAreaRepositoryAdapter implements ServiceAreaRepositoryPort {
    
    private final SpringDataServiceAreaRepository jpaRepository;
    private final ServiceAreaMapper mapper;
    
    public JpaServiceAreaRepositoryAdapter(SpringDataServiceAreaRepository jpaRepository,
                                          ServiceAreaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public ServiceArea save(ServiceArea serviceArea) {
        var jpaEntity = mapper.toJpaEntity(serviceArea);
        var saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<ServiceArea> findById(ServiceAreaId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public List<ServiceArea> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ServiceArea> findActiveByTenantId(TenantId tenantId) {
        return jpaRepository.findActiveByTenantId(tenantId.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTenantId(TenantId tenantId) {
        return jpaRepository.existsByTenantId(tenantId.getValue());
    }
    
    @Override
    public void delete(ServiceAreaId id) {
        jpaRepository.deleteById(id.getValue());
    }
}

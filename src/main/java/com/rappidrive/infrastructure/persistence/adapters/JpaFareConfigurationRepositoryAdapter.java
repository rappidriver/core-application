package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.FareConfigurationRepositoryPort;
import com.rappidrive.domain.entities.FareConfiguration;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.config.CacheConfiguration;
import com.rappidrive.infrastructure.persistence.entities.FareConfigurationJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.FareConfigurationMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataFareConfigurationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing FareConfigurationRepositoryPort using JPA.
 */
@Component
public class JpaFareConfigurationRepositoryAdapter implements FareConfigurationRepositoryPort {
    
    private final SpringDataFareConfigurationRepository jpaRepository;
    private final FareConfigurationMapper mapper;
    
    public JpaFareConfigurationRepositoryAdapter(
            SpringDataFareConfigurationRepository jpaRepository,
            FareConfigurationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    @CacheEvict(cacheNames = CacheConfiguration.FARE_CONFIG_CACHE, key = "#fareConfiguration.getTenantId().getValue()")
    public FareConfiguration save(FareConfiguration fareConfiguration) {
        FareConfigurationJpaEntity entity = mapper.toJpaEntity(fareConfiguration);
        FareConfigurationJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<FareConfiguration> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    @Cacheable(cacheNames = CacheConfiguration.FARE_CONFIG_CACHE, key = "#tenantId.getValue()")
    public Optional<FareConfiguration> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByTenantId(TenantId tenantId) {
        return jpaRepository.existsByTenantId(tenantId.getValue());
    }
    
    @Override
    @CacheEvict(cacheNames = CacheConfiguration.FARE_CONFIG_CACHE, key = "#fareConfiguration.getTenantId().getValue()")
    public void delete(FareConfiguration fareConfiguration) {
        FareConfigurationJpaEntity entity = mapper.toJpaEntity(fareConfiguration);
        jpaRepository.delete(entity);
    }
}

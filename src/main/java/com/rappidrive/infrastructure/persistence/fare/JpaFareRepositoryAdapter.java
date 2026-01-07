package com.rappidrive.infrastructure.persistence.fare;

import com.rappidrive.application.ports.output.FareRepositoryPort;
import com.rappidrive.domain.entities.Fare;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.FareJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA adapter for Fare repository port.
 */
@Component
public class JpaFareRepositoryAdapter implements FareRepositoryPort {
    
    private final SpringDataFareRepository jpaRepository;
    private final FareMapper mapper;
    
    public JpaFareRepositoryAdapter(SpringDataFareRepository jpaRepository, FareMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Fare save(Fare fare) {
        FareJpaEntity entity = mapper.toJpaEntity(fare);
        FareJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Fare> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Fare> findByTripId(UUID tripId) {
        return jpaRepository.findByTripId(tripId)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Fare> findByTenantIdAndCreatedAtBetween(
            TenantId tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return jpaRepository.findByTenantIdAndCreatedAtBetween(
                tenantId.getValue(),
                startDate,
                endDate
            )
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTripId(UUID tripId) {
        return jpaRepository.existsByTripId(tripId);
    }
}

package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.PaymentRepositoryPort;
import com.rappidrive.domain.entities.Payment;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.persistence.entities.PaymentJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.PaymentMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataPaymentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing PaymentRepositoryPort using JPA.
 */
@Component
public class JpaPaymentRepositoryAdapter implements PaymentRepositoryPort {
    
    private final SpringDataPaymentRepository jpaRepository;
    private final PaymentMapper mapper;
    
    public JpaPaymentRepositoryAdapter(
            SpringDataPaymentRepository jpaRepository,
            PaymentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toJpaEntity(payment);
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Payment> findByTripId(UUID tripId) {
        return jpaRepository.findByTripId(tripId)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Payment> findByTenantIdAndCreatedAtBetween(
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
    public List<Payment> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantIdOrderByCreatedAtDesc(tenantId.getValue())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByTripId(UUID tripId) {
        return jpaRepository.existsByTripId(tripId);
    }
    
    @Override
    public void delete(Payment payment) {
        PaymentJpaEntity entity = mapper.toJpaEntity(payment);
        jpaRepository.delete(entity);
    }
}

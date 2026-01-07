package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.application.ports.output.NotificationRepositoryPort;
import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;
import com.rappidrive.infrastructure.persistence.entities.NotificationJpaEntity;
import com.rappidrive.infrastructure.persistence.mappers.NotificationMapper;
import com.rappidrive.infrastructure.persistence.repositories.SpringDataNotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter JPA para NotificationRepositoryPort.
 */
@Component
public class JpaNotificationRepositoryAdapter implements NotificationRepositoryPort {
    
    private final SpringDataNotificationRepository jpaRepository;
    private final NotificationMapper mapper;
    
    public JpaNotificationRepositoryAdapter(
            SpringDataNotificationRepository jpaRepository,
            NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = mapper.toJpaEntity(notification);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Notification> findByIdempotencyKey(String idempotencyKey, TenantId tenantId) {
        return jpaRepository
            .findByIdempotencyKeyAndTenantIdAndDeletedAtIsNull(idempotencyKey, tenantId.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Notification> findByUserId(UserId userId, TenantId tenantId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return jpaRepository
            .findByUserIdAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                userId.getValue(),
                tenantId.getValue(),
                pageable
            )
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public List<Notification> findByUserIdAndStatus(
            UserId userId, 
            NotificationStatus status, 
            TenantId tenantId, 
            int page, 
            int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return jpaRepository
            .findByUserIdAndStatusAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                userId.getValue(),
                status.name(),
                tenantId.getValue(),
                pageable
            )
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
    
    @Override
    public Long countUnreadByUserId(UserId userId, TenantId tenantId) {
        Long count = jpaRepository.countUnreadByUserId(userId.getValue(), tenantId.getValue());
        return count != null ? count : 0L;
    }
    
    @Override
    public void deleteOldNotifications(LocalDateTime cutoffDate) {
        // Lidas: 30 dias após leitura
        LocalDateTime readCutoff = LocalDateTime.now().minusDays(30);
        
        // Não lidas: 90 dias após criação
        LocalDateTime oldCutoff = LocalDateTime.now().minusDays(90);
        
        // Falhas: 7 dias após falha
        LocalDateTime failedCutoff = LocalDateTime.now().minusDays(7);
        
        jpaRepository.softDeleteOldNotifications(readCutoff, oldCutoff, failedCutoff);
    }
    
    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey, TenantId tenantId) {
        return jpaRepository.existsByIdempotencyKeyAndTenantIdAndDeletedAtIsNull(
            idempotencyKey, 
            tenantId.getValue()
        );
    }
}

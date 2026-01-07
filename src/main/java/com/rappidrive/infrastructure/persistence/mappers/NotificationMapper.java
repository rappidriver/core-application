package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationPriority;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.enums.NotificationType;
import com.rappidrive.domain.valueobjects.NotificationContent;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;
import com.rappidrive.infrastructure.persistence.entities.NotificationJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Notification (domain) e NotificationJpaEntity (infrastructure).
 */
@Component
public class NotificationMapper {
    
    /**
     * Converte domain entity para JPA entity
     */
    public NotificationJpaEntity toJpaEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId().getValue());
        entity.setType(notification.getType().name());
        entity.setPriority(notification.getPriority().name());
        entity.setTitle(notification.getContent().getTitle());
        entity.setMessage(notification.getContent().getMessage());
        entity.setData(notification.getContent().getData());
        entity.setStatus(notification.getStatus().name());
        entity.setIdempotencyKey(notification.getIdempotencyKey());
        entity.setTenantId(notification.getTenantId().getValue());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setSentAt(notification.getSentAt());
        entity.setReadAt(notification.getReadAt());
        entity.setFailedAt(notification.getFailedAt());
        return entity;
    }
    
    /**
     * Converte JPA entity para domain entity
     */
    public Notification toDomain(NotificationJpaEntity entity) {
        NotificationContent content = NotificationContent.of(
            entity.getTitle(),
            entity.getMessage(),
            entity.getData()
        );
        
        return Notification.builder()
            .id(entity.getId())
            .userId(new UserId(entity.getUserId()))
            .type(NotificationType.valueOf(entity.getType()))
            .priority(NotificationPriority.valueOf(entity.getPriority()))
            .content(content)
            .status(NotificationStatus.valueOf(entity.getStatus()))
            .idempotencyKey(entity.getIdempotencyKey())
            .tenantId(new TenantId(entity.getTenantId()))
            .createdAt(entity.getCreatedAt())
            .sentAt(entity.getSentAt())
            .readAt(entity.getReadAt())
            .failedAt(entity.getFailedAt())
            .build();
    }
}

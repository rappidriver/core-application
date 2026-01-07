package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.NotificationPriority;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.enums.NotificationType;
import com.rappidrive.domain.exceptions.InvalidNotificationException;
import com.rappidrive.domain.exceptions.InvalidNotificationStatusException;
import com.rappidrive.domain.valueobjects.NotificationContent;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representando uma notificação enviada a um usuário.
 * Notificações são imutáveis após criação, apenas status pode mudar.
 */
public class Notification {
    
    private final UUID id;
    private final UserId userId;
    private final NotificationType type;
    private final NotificationPriority priority;
    private final NotificationContent content;
    private NotificationStatus status;
    private final String idempotencyKey;
    private final TenantId tenantId;
    private final LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private LocalDateTime failedAt;
    
    // Construtor privado - use factory methods
    private Notification(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.userId = Objects.requireNonNull(builder.userId, "UserId não pode ser nulo");
        this.type = Objects.requireNonNull(builder.type, "NotificationType não pode ser nulo");
        this.priority = builder.priority != null 
            ? builder.priority 
            : NotificationPriority.fromType(builder.type);
        this.content = Objects.requireNonNull(builder.content, "NotificationContent não pode ser nulo");
        this.status = builder.status != null ? builder.status : NotificationStatus.PENDING;
        this.idempotencyKey = builder.idempotencyKey;
        this.tenantId = Objects.requireNonNull(builder.tenantId, "TenantId não pode ser nulo");
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.sentAt = builder.sentAt;
        this.readAt = builder.readAt;
        this.failedAt = builder.failedAt;
    }
    
    /**
     * Cria nova notificação com status PENDING
     */
    public static Notification create(
            UserId userId,
            NotificationType type,
            NotificationContent content,
            TenantId tenantId,
            String idempotencyKey) {
        return builder()
                .userId(userId)
                .type(type)
                .content(content)
                .tenantId(tenantId)
                .idempotencyKey(idempotencyKey)
                .build();
    }
    
    /**
     * Marca notificação como enviada (SENT)
     */
    public void markAsSent() {
        validateTransition(NotificationStatus.SENT);
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * Marca notificação como lida (READ)
     */
    public void markAsRead() {
        validateTransition(NotificationStatus.READ);
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Marca notificação como falha (FAILED)
     */
    public void markAsFailed() {
        validateTransition(NotificationStatus.FAILED);
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }
    
    /**
     * Valida se transição de status é permitida
     */
    private void validateTransition(NotificationStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidNotificationStatusException(
                String.format("Transição inválida de %s para %s", this.status, newStatus)
            );
        }
    }
    
    /**
     * Verifica se notificação está pendente
     */
    public boolean isPending() {
        return status.isPending();
    }
    
    /**
     * Verifica se notificação foi enviada
     */
    public boolean isSent() {
        return status.isSent();
    }
    
    /**
     * Verifica se notificação foi lida
     */
    public boolean isRead() {
        return status.isRead();
    }
    
    /**
     * Verifica se notificação falhou
     */
    public boolean isFailed() {
        return status.isFailed();
    }
    
    /**
     * Verifica se notificação não foi lida
     */
    public boolean isUnread() {
        return status.isUnread();
    }
    
    /**
     * Verifica se é alta prioridade
     */
    public boolean isHighPriority() {
        return priority.isHigh();
    }
    
    /**
     * Verifica se possui chave de idempotência
     */
    public boolean hasIdempotencyKey() {
        return idempotencyKey != null && !idempotencyKey.isBlank();
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public NotificationPriority getPriority() {
        return priority;
    }
    
    public NotificationContent getContent() {
        return content;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    
    public TenantId getTenantId() {
        return tenantId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public LocalDateTime getFailedAt() {
        return failedAt;
    }
    
    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID id;
        private UserId userId;
        private NotificationType type;
        private NotificationPriority priority;
        private NotificationContent content;
        private NotificationStatus status;
        private String idempotencyKey;
        private TenantId tenantId;
        private LocalDateTime createdAt;
        private LocalDateTime sentAt;
        private LocalDateTime readAt;
        private LocalDateTime failedAt;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder userId(UserId userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }
        
        public Builder priority(NotificationPriority priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder content(NotificationContent content) {
            this.content = content;
            return this;
        }
        
        public Builder status(NotificationStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }
        
        public Builder tenantId(TenantId tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }
        
        public Builder readAt(LocalDateTime readAt) {
            this.readAt = readAt;
            return this;
        }
        
        public Builder failedAt(LocalDateTime failedAt) {
            this.failedAt = failedAt;
            return this;
        }
        
        public Notification build() {
            return new Notification(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", type=" + type +
                ", priority=" + priority +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}

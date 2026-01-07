package com.rappidrive.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entidade JPA para notificações.
 */
@Entity
@Table(name = "notifications", 
    indexes = {
        @Index(name = "idx_notifications_user_id_status", columnList = "user_id, status"),
        @Index(name = "idx_notifications_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_notifications_type_tenant", columnList = "type, tenant_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_notifications_idempotency_key", columnNames = "idempotency_key")
    }
)
public class NotificationJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "priority", nullable = false, length = 20)
    private String priority;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "message", nullable = false, length = 500)
    private String message;
    
    @Column(name = "data", columnDefinition = "jsonb")
    @Convert(converter = JsonbMapConverter.class)
    private Map<String, String> data;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, String> getData() {
        return data;
    }
    
    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }
    
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public LocalDateTime getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Converter para JSONB do PostgreSQL
     */
    @jakarta.persistence.Converter(autoApply = false)
    public static class JsonbMapConverter implements jakarta.persistence.AttributeConverter<Map<String, String>, String> {
        
        private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = 
            new com.fasterxml.jackson.databind.ObjectMapper();
        
        @Override
        public String convertToDatabaseColumn(Map<String, String> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error converting map to JSON", e);
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Map<String, String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) {
                return Map.of();
            }
            try {
                return objectMapper.readValue(dbData, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error converting JSON to map", e);
            }
        }
    }
}

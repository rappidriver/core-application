package com.rappidrive.application.ports.output;

import com.rappidrive.domain.entities.Notification;
import com.rappidrive.domain.enums.NotificationStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de notificações.
 */
public interface NotificationRepositoryPort {
    
    /**
     * Salva notificação
     */
    Notification save(Notification notification);
    
    /**
     * Busca notificação por ID
     */
    Optional<Notification> findById(UUID id);
    
    /**
     * Busca notificação por chave de idempotência
     */
    Optional<Notification> findByIdempotencyKey(String idempotencyKey, TenantId tenantId);
    
    /**
     * Busca todas notificações de um usuário (paginado)
     */
    List<Notification> findByUserId(UserId userId, TenantId tenantId, int page, int size);
    
    /**
     * Busca notificações de um usuário filtradas por status (paginado)
     */
    List<Notification> findByUserIdAndStatus(
        UserId userId, 
        NotificationStatus status, 
        TenantId tenantId, 
        int page, 
        int size
    );
    
    /**
     * Conta notificações não lidas de um usuário
     */
    Long countUnreadByUserId(UserId userId, TenantId tenantId);
    
    /**
     * Deleta notificações antigas (soft-delete)
     */
    void deleteOldNotifications(LocalDateTime cutoffDate);
    
    /**
     * Verifica se existe notificação com chave de idempotência
     */
    boolean existsByIdempotencyKey(String idempotencyKey, TenantId tenantId);
}
